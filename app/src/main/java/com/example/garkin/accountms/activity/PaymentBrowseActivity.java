package com.example.garkin.accountms.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garkin.accountms.R;
import com.example.garkin.accountms.dao.PaymentDao;
import com.example.garkin.accountms.model.Payment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentBrowseActivity extends AppCompatActivity {
    //调用编辑支出信息和新增支出信息的申请码
    private static final int REQUEST_CODE_EDIT_PAYMENT = 0x01;
    private static final int REQUEST_CODE_ADD_PAYMENT = 0x02;

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final int ROWS_OF_PAGE = 10; //每页显示的记录数
    private static PaymentDao paymentDao;
    private static ActionMode actionMode;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView paymentListView;
    private CheckBox paymentSelectAllCheckBox;   //全选CheckBox
    private List<Payment> paymentList = new ArrayList<>();
    private PaymentBaseAdapter paymentBaseAdapter;
    //分页显示变量,从0到totalPage-1页
    private int currentPage = 0;
    private int totalPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_browse);
        // 获取布局文件中的ListView组件
        paymentListView = (ListView) findViewById(R.id.browsePaymentListView);
        if (paymentListView == null) return;
        //设置ListView为多选Modal模式
        paymentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //mCallback为ListView多选模式监听器
        ModeCallback mCallback = new ModeCallback();
        paymentListView.setMultiChoiceModeListener(mCallback);
        //获取全选CheckBox组件
        paymentSelectAllCheckBox = (CheckBox) findViewById(R.id.browsePaymentSelectAllCheckbox);
        //处理进入Action Mode方式后点击全选CheckBox事件,选中当前页所有支出信息
        paymentSelectAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentSelectAllCheckBox.isChecked()) {
                    for (int i = 0; i < paymentBaseAdapter.getCount(); i++)
                        paymentListView.setItemChecked(i, true);
                } else {
                    paymentListView.clearChoices();
                    paymentListView.setItemChecked(0, false);
                }
            }
        });

        //处理单击列表项事件,进入编辑支出信息
        paymentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PaymentBrowseActivity.this, PaymentEditActivity.class);
                intent.putExtra(Payment.class.getName(), paymentList.get(position));
                startActivityForResult(intent, REQUEST_CODE_EDIT_PAYMENT);
            }
        });

        //处理下拉刷新支出信息，显示下一页数据
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.browsePaymentSwipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            //设置SwipeRefreshLayout动画颜色，可以设置4个
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                    android.R.color.holo_orange_light, android.R.color.holo_green_light);
            swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);//设置进度圈的大小
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    //加载下一页支出信息
                    loadNextPageData();
                    Toast.makeText(PaymentBrowseActivity.this, "共" + totalPage + "页" + ",当前第" + (currentPage + 1) + "页", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //刷新完成
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 500);
                }
            });
            //解决ListView与SwipeRefreshLayout滑动冲突问题
            paymentListView.setOnScrollListener(new SwipeListViewOnScrollListener(swipeRefreshLayout));
        }

        //设置应用图标导航
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        //加载当前页数据
        loadCurrentPageData();
    }

    //加载当前页数据
    public void loadCurrentPageData() {
        //获取PaymentDao
        if (paymentDao == null) paymentDao = PaymentDao.getPaymentDaoInstance(this);
        //计算总页数
        totalPage = paymentDao.getCount() / ROWS_OF_PAGE + (paymentDao.getCount() % ROWS_OF_PAGE > 0 ? 1 : 0);
        //判断当前页是否合法，在删除记录时，当前页可能已不存在，当前页不存在，设置当前页为0
        if (currentPage >= totalPage) currentPage = 0;
        //重新获取支出信息记录集合
        paymentList = paymentDao.getPaymentList(currentPage * ROWS_OF_PAGE + 1, ROWS_OF_PAGE);
        //绑定ListView的Adapter,刷新ListView
        if (paymentBaseAdapter == null) {
            paymentBaseAdapter = new PaymentBaseAdapter(this);
            paymentListView.setAdapter(paymentBaseAdapter);
        } else
            paymentBaseAdapter.notifyDataSetChanged();
    }

    //加载下一页数据
    public void loadNextPageData() {
        if (currentPage == totalPage - 1) {
            currentPage = 0;
            paymentList = paymentDao.getPaymentList(currentPage * ROWS_OF_PAGE + 1, ROWS_OF_PAGE);
        } else if (currentPage < totalPage - 1) {
            paymentList = paymentDao.getPaymentList(++currentPage * ROWS_OF_PAGE + 1, ROWS_OF_PAGE);
        }
        paymentBaseAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_payment_browse, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_payment_browse_new:
                startActivityForResult(new Intent(PaymentBrowseActivity.this, PaymentEditActivity.class), REQUEST_CODE_ADD_PAYMENT);// 打开AddPaymentActivity
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //startActivityForResult()的回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_PAYMENT:
            case REQUEST_CODE_ADD_PAYMENT:
                //编辑或新增支出信息后，需要重新加载当前页面，因为当前页面的内容可能更新了
                loadCurrentPageData();
                break;
            default:
                break;
        }
    }

    private static class PaymentViewHolder {
        public TextView paymentIdTextView; // 支出编号
        public TextView paymentDateTextView; // 支出日期
        public TextView paymentAmountTextView; // 支出金额
        public TextView paymentTypeTextView; // 支出类别
        public CheckBox paymentCheckBox;     //是否选中
    }

    //下拉ListView监听器,判断ListView下拉时是否滑动到列表顶部，是否允许SwipeView滑动事件，否则禁止触发SwipeView下拉刷新
    public static class SwipeListViewOnScrollListener implements AbsListView.OnScrollListener {
        private SwipeRefreshLayout mSwipeView;
        private AbsListView.OnScrollListener mOnScrollListener;

        public SwipeListViewOnScrollListener(SwipeRefreshLayout swipeView) {
            mSwipeView = swipeView;
        }

        public SwipeListViewOnScrollListener(SwipeRefreshLayout swipeView,
                                             AbsListView.OnScrollListener onScrollListener) {
            mSwipeView = swipeView;
            mOnScrollListener = onScrollListener;
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            View firstView = absListView.getChildAt(firstVisibleItem);

            // 当firstVisibleItem是第0位。如果firstView==null说明列表为空，
            // 需要刷新;或者top==0说明已经到达列表顶部, 也需要刷新
            //当ActionMode为空时允许刷新,否则在ActionMode模式下，禁止刷新
            if (firstVisibleItem == 0 && (firstView == null || firstView.getTop() == 0) && actionMode == null) {
                mSwipeView.setEnabled(true);
            } else {
                mSwipeView.setEnabled(false);
            }
            if (null != mOnScrollListener) {
                mOnScrollListener.onScroll(absListView, firstVisibleItem,
                        visibleItemCount, totalItemCount);
            }
        }
    }

    //ListView多选模式监听器
    private class ModeCallback implements AbsListView.MultiChoiceModeListener {

        /**
         * Called when an item is checked or unchecked during selection mode.
         */
        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            mode.invalidate();
            if (paymentBaseAdapter != null) paymentBaseAdapter.notifyDataSetChanged();
        }

        /**
         * Called when action mode is first created. The menu supplied will be used to
         * generate action buttons for the action mode.
         */
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // ActionMode的菜单处理
            getMenuInflater().inflate(R.menu.menu_payment_browse_context, menu);
            actionMode = mode;
            return true;
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated.
         */
        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            paymentSelectAllCheckBox.setVisibility(View.VISIBLE);
            paymentBaseAdapter.setCheckBoxVisibility(View.VISIBLE);
            paymentBaseAdapter.notifyDataSetChanged();
            mode.setTitle("完成");
            return true;
        }

        /**
         * Called to report a user click on an action button.
         */
        @Override
        public boolean onActionItemClicked(final android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_payment_browse_delete:
                    // retrieve selected item and delete it out
                    new AlertDialog.Builder(PaymentBrowseActivity.this).setIcon(android.R.drawable.ic_delete)
                            .setMessage("确认删除已选中的支出信息吗？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (paymentDao == null)
                                        paymentDao = PaymentDao.getPaymentDaoInstance(PaymentBrowseActivity.this);
                                    long[] selectedIds = paymentListView.getCheckedItemIds();
                                    if (paymentDao.delete(selectedIds) > 0) {
                                        //删除支出信息后，需要重新加载当前页面，因为页面总数和当前页的内容可能删除了
                                        loadCurrentPageData();
                                    }
                                    mode.finish();
                                }
                            }).create().show();
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Called when an action mode is about to be exited and destroyed.
         */
        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            paymentListView.clearChoices();
            paymentSelectAllCheckBox.setVisibility(View.GONE);
            paymentBaseAdapter.setCheckBoxVisibility(View.GONE);
            paymentBaseAdapter.notifyDataSetChanged();
            actionMode = null;
        }
    }

    //ListView适配器
    private class PaymentBaseAdapter extends BaseAdapter {
        private int checkBoxVisibility = View.GONE;
        //设置CheckBox默认不可见,在ActionMode的onPrepareActionMode 设置可见,在ActionMode的onDestroyActionMode又设置回不可见
        private LayoutInflater mInflater = null;
        private PaymentViewHolder paymentViewHolder;

        public PaymentBaseAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (paymentList == null)
                return 0;
            return paymentList.size();
        }

        @Override
        public Payment getItem(int position) {
            if (paymentList == null)
                return null;
            return paymentList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (paymentList == null)
                return 0;
            return paymentList.get(position).getId();
        }

        //hasStableIds返回true时，ListView的getCheckedItemIds才有效
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                paymentViewHolder = new PaymentViewHolder();
                convertView = mInflater.inflate(R.layout.item_listview_payment_browse, parent, false);
                paymentViewHolder.paymentIdTextView = (TextView) convertView
                        .findViewById(R.id.itemPaymentIdTextView);
                paymentViewHolder.paymentDateTextView = (TextView) convertView
                        .findViewById(R.id.itemPaymentDateTextView);
                paymentViewHolder.paymentAmountTextView = (TextView) convertView
                        .findViewById(R.id.itemPaymentAmountTextView);
                paymentViewHolder.paymentTypeTextView = (TextView) convertView
                        .findViewById(R.id.itemPaymentTypeTextView);
                paymentViewHolder.paymentCheckBox = (CheckBox) convertView.findViewById(R.id.itemPaymentCheckBox);
                convertView.setTag(paymentViewHolder);
            } else {
                paymentViewHolder = (PaymentViewHolder) convertView.getTag();
            }
            Payment payment = paymentList.get(position);
            paymentViewHolder.paymentIdTextView.setText(String.valueOf(payment.getId()));
            paymentViewHolder.paymentDateTextView.setText(dateFormat.format(payment.getTime()));
            paymentViewHolder.paymentAmountTextView.setText(String.valueOf(payment.getAmount()));
            paymentViewHolder.paymentTypeTextView.setText(payment.getType());
            paymentViewHolder.paymentCheckBox.setChecked(paymentListView.isItemChecked(position));
            paymentViewHolder.paymentCheckBox.setVisibility(checkBoxVisibility);
            return convertView;
        }

        public void setCheckBoxVisibility(int checkBoxVisibility) {
            this.checkBoxVisibility = checkBoxVisibility;
        }
    }
}
