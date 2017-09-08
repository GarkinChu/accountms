package com.example.garkin.accountms.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.garkin.accountms.R;
import com.example.garkin.accountms.dao.IncomeDao;
import com.example.garkin.accountms.dao.PaymentDao;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class UseFinanceActivity extends AppCompatActivity {
    protected BarChart mChart;
    ArrayList<BarEntry> yValues1;
    ArrayList<BarEntry> yValues2;
    ArrayList<String> xValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
            }

            @Override
            public void onNothingSelected() {
            }
        });
        //setDrawBarShadow(boolean enabled): If set to true, a grey area is drawn behind each bar
        // that indicates the maximum value. Enabling his will reduce performance by about 40%.
        mChart.setDrawBarShadow(false);
        //setDrawValueAboveBar(boolean enabled): If set to true, all values are drawn above
        // their bars, instead of below their top.
        mChart.setDrawValueAboveBar(true);
        // setDrawValuesForWholeStack(boolean enabled): If set to true, all values of stacked bars
        // are drawn individually, and not just their sum on top of all.
//        mChart.setDrawValuesForWholeStack(true);
        mChart.setDrawHighlightArrow(true);
        mChart.setDescription("");
//        mChart.setDescriptionPosition(mChart.getMeasuredWidth() / 2, mChart.getMeasuredHeight() / 2);
//        Toast.makeText(this, "" + mChart.getMeasuredWidth(), Toast.LENGTH_LONG).show();
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);
        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        YAxisValueFormatter custom = new MyYAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(10f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        setData();

        // mChart.setDrawLegend(false);
    }

    private void setData() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        IncomeDao incomeDao = IncomeDao.getIncomeDaoInstance(this);
        PaymentDao paymentDao = PaymentDao.getPaymentDaoInstance(this);
        //获取收入与支出最大最小日期
        Date incomeMinDate = incomeDao.getMinDate();
        Date incomeMaxDate = incomeDao.getMaxDate();
        Date paymentMinDate = paymentDao.getMinDate();
        Date paymentMaxDate = paymentDao.getMaxDate();
        //需要判断空日期
        //无数据
        if (incomeMinDate == null && paymentMinDate == null) return;
        Date minDate, maxDate;
        //获取最小日期
        if (incomeMinDate == null) minDate = paymentMinDate;
        else if (paymentMinDate == null) minDate = incomeMinDate;
        else minDate = incomeMinDate.after(paymentMinDate) ? paymentMinDate : incomeMinDate;
        //获取最大日期
        if (incomeMaxDate == null) maxDate = paymentMaxDate;
        else if (paymentMaxDate == null) maxDate = incomeMaxDate;
        else maxDate = incomeMaxDate.after(paymentMaxDate) ? incomeMaxDate : paymentMaxDate;

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(minDate);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(maxDate);
        endCalendar.set(Calendar.DAY_OF_MONTH, 1);

        xValues = new ArrayList<>();
        yValues1 = new ArrayList<>();
        yValues2 = new ArrayList<>();
        int i = 0;
        while (!startCalendar.getTime().after(endCalendar.getTime())) {
            xValues.add(i, dateFormat.format(startCalendar.getTime()));
            Date date = startCalendar.getTime();
            yValues1.add(new BarEntry(paymentDao.getSumMonthAmount(date), i));
            yValues2.add(new BarEntry(incomeDao.getSumMonthAmount(date), i));
            startCalendar.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH) + 1);
            i++;
        }

        BarDataSet set1;
        BarDataSet set2;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 1) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setYVals(yValues1);
            set2 = (BarDataSet) mChart.getData().getDataSetByIndex(1);
            set2.setYVals(yValues2);
            mChart.getData().setXVals(xValues);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yValues1, "支出");
            set1.setBarSpacePercent(35f);
            set1.setColor(Color.RED);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            set2 = new BarDataSet(yValues2, "收入");
            set2.setBarSpacePercent(35f);
            set2.setColor(Color.GREEN);
            dataSets.add(set2);

            BarData data = new BarData(xValues, dataSets);
            data.setValueTextSize(10f);
            // add space between the DataSet groups in percent of bar-width
            data.setGroupSpace(40f);
            mChart.setData(data);
            mChart.setVisibleXRange(6, 8);
            mChart.moveViewToX(2 * xValues.size());
        }
    }

    public class MyYAxisValueFormatter implements YAxisValueFormatter {

        private DecimalFormat mFormat;

        public MyYAxisValueFormatter() {
            mFormat = new DecimalFormat("###,###,###,##0");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return "￥" + mFormat.format(value);
        }
    }
}
