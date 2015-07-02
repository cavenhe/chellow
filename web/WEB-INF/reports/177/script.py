import os
import traceback
from net.sf.chellow.monad import Monad
import datetime
from dateutil.relativedelta import relativedelta
import pytz
from sqlalchemy import or_, cast, Float, func
from sqlalchemy.sql.expression import null, true
import time
import utils
import db
import computer
import threading
import dloads
Monad.getUtils()['impt'](globals(), 'computer', 'db', 'utils', 'dloads')

HH, hh_format, hh_after = utils.HH, utils.hh_format, utils.hh_after
totalseconds, UserException = utils.totalseconds, utils.UserException
Supply, Era, Site, SiteEra = db.Supply, db.Era, db.Site, db.SiteEra
HhDatum, Channel, Bill = db.HhDatum, db.Channel, db.Bill
inv = globals()['inv']

year = inv.getInteger("end_year")
month = inv.getInteger("end_month")
months = inv.getInteger("months")
if inv.hasParameter('supply_id'):
    supply_id = inv.getLong('supply_id')
else:
    supply_id = None

user = inv.getUser()


def content():
    sess = None
    tmp_file = None
    try:
        sess = db.session()
        supplies = sess.query(Supply).join(Era).distinct()

        if supply_id is None:
            base_name = "supplies_monthly_duration_for_all_supplies_for_" + \
                str(months) + "_to_" + str(year) + "_" + str(month) + ".csv"
        else:
            supply = Supply.get_by_id(sess, supply_id)
            supplies = supplies.filter(Supply.id == supply.id)
            base_name = "supplies_monthly_duration_for_" + str(supply.id) + \
                "_" + str(months) + "_to_" + str(year) + "_" + str(month) + \
                ".csv"
        running_name, finished_name = dloads.make_names(base_name, user)

        tmp_file = open(running_name, "w")

        caches = {}

        start_date = datetime.datetime(year, month, 1, tzinfo=pytz.utc) - \
            relativedelta(months=months-1)

        field_names = (
            'supply-name', 'source-code', 'generator-type', 'month', 'pc-code',
            'msn', 'site-code', 'site-name', 'metering-type',
            'import-mpan-core', 'metered-import-kwh', 'metered-import-net-gbp',
            'metered-import-estimated-kwh', 'billed-import-kwh',
            'billed-import-net-gbp', 'export-mpan-core', 'metered-export-kwh',
            'metered-export-estimated-kwh', 'billed-export-kwh',
            'billed-export-net-gbp', 'problem', 'timestamp')

        tmp_file.write('supply-id,' + ','.join(field_names) + '\n')

        forecast_date = computer.forecast_date()

        for i in range(months):
            month_start = start_date + relativedelta(months=i)
            month_finish = month_start + relativedelta(months=1) - HH

            for supply in supplies.filter(
                    Era.start_date <= month_finish,
                    or_(
                        Era.finish_date == null(),
                        Era.finish_date >= month_start)):

                generator_type = supply.generator_type
                if generator_type is None:
                    generator_type = ''
                else:
                    generator_type = generator_type.code

                source_code = supply.source.code
                eras = supply.find_eras(sess, month_start, month_finish)
                era = eras[-1]
                metering_type = era.make_meter_category()

                site = sess.query(Site).join(SiteEra).filter(
                    SiteEra.era == era, SiteEra.is_physical == true()).one()

                values = {
                    'supply-name': supply.name, 'source-code': source_code,
                    'generator-type': generator_type,
                    'month': hh_format(month_finish), 'pc-code': era.pc.code,
                    'msn': era.msn, 'site-code': site.code,
                    'site-name': site.name, 'metering-type': metering_type,
                    'problem': ''}

                tmp_file.write(str(supply.id) + ',')

                for is_import, pol_name in [
                        (True, 'import'), (False, 'export')]:
                    if is_import:
                        mpan_core = era.imp_mpan_core
                    else:
                        mpan_core = era.exp_mpan_core

                    values[pol_name + '-mpan-core'] = mpan_core
                    kwh = 0
                    est_kwh = 0

                    if metering_type in ['hh', 'amr']:
                        est_kwh = sess.query(HhDatum.value).join(Channel) \
                            .join(Era).filter(
                                HhDatum.status == 'E',
                                Era.supply_id == supply.id,
                                Channel.channel_type == 'ACTIVE',
                                Channel.imp_related == is_import,
                                HhDatum.start_date >= month_start,
                                HhDatum.start_date <= month_finish).first()
                        if est_kwh is None:
                            est_kwh = 0
                        else:
                            est_kwh = est_kwh[0]

                    if not (is_import and source_code in ('net', 'gen-net')):
                        kwh_sum = sess.query(
                            cast(func.sum(HhDatum.value), Float)
                            ).join(Channel).join(Era).filter(
                            Era.supply_id == supply.id,
                            Channel.channel_type == 'ACTIVE',
                            Channel.imp_related == is_import,
                            HhDatum.start_date >= month_start,
                            HhDatum.start_date <= month_finish).one()[0]
                        if kwh_sum is not None:
                            kwh += kwh_sum

                    values['metered-' + pol_name + '-estimated-kwh'] = est_kwh
                    values['metered-' + pol_name + '-kwh'] = kwh
                    values['metered-' + pol_name + '-net-gbp'] = 0
                    values['billed-' + pol_name + '-kwh'] = 0
                    values['billed-' + pol_name + '-net-gbp'] = 0
                    values['billed-' + pol_name + '-apportioned-kwh'] = 0
                    values['billed-' + pol_name + '-apportioned-net-gbp'] = 0
                    values['billed-' + pol_name + '-raw-kwh'] = 0
                    values['billed-' + pol_name + '-raw-net-gbp'] = 0

                for bill in sess.query(Bill).filter(
                        Bill.supply == supply, Bill.start_date <= month_finish,
                        Bill.finish_date >= month_start):
                    bill_start = bill.start_date
                    bill_finish = bill.finish_date
                    bill_duration = totalseconds(
                        bill_finish - bill_start) + 30 * 60
                    overlap_duration = totalseconds(
                        min(bill_finish, month_finish) -
                        max(bill_start, month_start)) + 30 * 60
                    overlap_proportion = float(
                        overlap_duration) / float(bill_duration)
                    values['billed-import-net-gbp'] += \
                        overlap_proportion * float(bill.net)
                    values['billed-import-kwh'] += \
                        overlap_proportion * float(bill.kwh)

                for era in eras:
                    if era.start_date > month_start:
                        chunk_start = era.start_date
                    else:
                        chunk_start = month_start
                    if hh_after(era.finish_date, month_finish):
                        chunk_finish = month_finish
                    else:
                        chunk_finish = era.finish_date

                    import_mpan_core = era.imp_mpan_core
                    if import_mpan_core is None:
                        continue

                    supplier_contract = era.imp_supplier_contract

                    if source_code in ['net', 'gen-net', '3rd-party']:
                        supply_source = computer.SupplySource(
                            sess, chunk_start, chunk_finish, forecast_date,
                            era, True, None, caches)

                        values['metered-import-kwh'] += sum(
                            datum['msp-kwh']
                            for datum in supply_source.hh_data)

                        import_vb_function = supply_source.contract_func(
                            supplier_contract, 'virtual_bill')
                        if import_vb_function is None:
                            values['problem'] += "Can't find the " \
                                "virtual_bill  function in the supplier " \
                                "contract. "
                        else:
                            import_vb_function(supply_source)
                            values['metered-import-net-gbp'] += \
                                supply_source.supplier_bill['net-gbp']

                        supply_source.contract_func(
                            era.hhdc_contract, 'virtual_bill')(supply_source)
                        values['metered-import-net-gbp'] += \
                            supply_source.dc_bill['net-gbp']

                        mop_func = supply_source.contract_func(
                            era.mop_contract, 'virtual_bill')
                        if mop_func is None:
                            values['problem'] += " MOP virtual_bill " \
                                "function can't be found."
                        else:
                            mop_func(supply_source)
                            mop_bill = supply_source.mop_bill
                            values['metered-import-net-gbp'] += \
                                mop_bill['net-gbp']
                            if len(mop_bill['problem']) > 0:
                                values['problem'] += \
                                    " MOP virtual bill problem: " + \
                                    mop_bill['problem']

                values['timestamp'] = int(time.time() * 1000)
                tmp_file.write(
                    ','.join(
                        '"' + str(values[name]) +
                        '"' for name in field_names) + '\n')
    except:
        tmp_file.write(traceback.format_exc())
    finally:
        if sess is not None:
            sess.close()
        tmp_file.close()
        os.rename(running_name, finished_name)

threading.Thread(target=content).start()
inv.sendSeeOther("/reports/251/output/")
