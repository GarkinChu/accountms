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
import com.example.garkin.accountms.dao.NoteDao;
import com.example.garkin.accountms.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteBrowseActivity extends AppCompatActivity {
    //调用编辑便签信息和新增便签信息的申请码
    private static final int REQUEST_CODE_EDIT_NOTE = 0x31;
    private static final int REQUEST_CODE_ADD_NOTE = 0x32;

    private static final int ROWS_OF_PAGE = 10; //每页显示的记录数
    private static NoteDao noteDao;
    private static ActionMode actionMode;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView noteListView;
    private CheckBox noteSelectAllCheckBox;   //全选CheckBox
    private List<Note> noteList = new ArrayList<>();
    private NoteBaseAdapter noteBaseAdapter;
    //分页显示变量,从0到totalPage-1页
    private int currentPage = 0;
    private int totalPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_browse);
        // 获取布局文件中的ListView组件
        noteListView = (ListView) findViewById(R.id.browseNoteListView);
        if (noteListView == null) return;
        //设置ListView为多选Modal模式
        noteListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //mCallback为ListView多选模式监听器
        ModeCallback mCallback = new ModeCallback();
        noteListView.setMultiChoiceModeListener(mCallback);
        //获取全选CheckBox组件
        noteSelectAllCheckBox = (CheckBox) findViewById(R.id.browseNoteSelectAllCheckbox);
        //处理进入Action Mode方式后点击全选CheckBox事件,选中当前页所有便签信息
        noteSelectAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteSelectAllCheckBox.isChecked()) {
                    for (int i = 0; i < noteBaseAdapter.getCount(); i++)
                        noteListView.setItemChecked(i, true);
                } else {
                    noteListView.clearChoices();
                    noteListView.setItemChecked(0, false);
                }
            }
        });

        //处理单击列表项事件,进入编辑便签信息
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NoteBrowseActivity.this, NoteEditActivity.class);
                intent.putExtra(Note.class.getName(), noteList.get(position));
                startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
            }
        });

        //处理下拉刷新便签信息，显示下一页数据
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.browseNoteSwipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            //设置SwipeRefreshLayout动画颜色，可以设置4个
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                    android.R.color.holo_orange_light, android.R.color.holo_green_light);
            swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);//设置进度圈的大小
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    //加载下一页便签信息
                    loadNextPageData();
                    Toast.makeText(NoteBrowseActivity.this, "共" + totalPage + "页" + ",当前第" + (currentPage + 1) + "页", Toast.LENGTH_SHORT).show();
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
            noteListView.setOnScrollListener(new SwipeListViewOnScrollListener(swipeRefreshLayout));
        }

        //设置应用图标导航
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        //加载当前页数据
        loadCurrentPageData();
    }

    //加载当前页数据
    public void loadCurrentPageData() {
        //获取NoteDao
        if (noteDao == null) noteDao = NoteDao.getNoteDaoInstance(this);
        //计算总页数
        totalPage = noteDao.getCount() / ROWS_OF_PAGE + (noteDao.getCount() % ROWS_OF_PAGE > 0 ? 1 : 0);
        //判断当前页是否合法，在删除记录时，当前页可能已不存在，当前页不存在，设置当前页为0
        if (currentPage >= totalPage) currentPage = 0;
        //重新获取便签信息记录集合
        noteList = noteDao.getNoteList(currentPage * ROWS_OF_PAGE + 1, ROWS_OF_PAGE);
        //绑定ListView的Adapter,刷新ListView
        if (noteBaseAdapter == null) {
            noteBaseAdapter = new NoteBaseAdapter(this);
            noteListView.setAdapter(noteBaseAdapter);
        } else
            noteBaseAdapter.notifyDataSetChanged();
    }

    //加载下一页数据
    public void loadNextPageData() {
        if (currentPage == totalPage - 1) {
            currentPage = 0;
            noteList = noteDao.getNoteList(currentPage * ROWS_OF_PAGE + 1, ROWS_OF_PAGE);
        } else if (currentPage < totalPage - 1) {
            noteList = noteDao.getNoteList(++currentPage * ROWS_OF_PAGE + 1, ROWS_OF_PAGE);
        }
        noteBaseAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_browse, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_note_browse_new:
                startActivityForResult(new Intent(NoteBrowseActivity.this, NoteEditActivity.class), REQUEST_CODE_ADD_NOTE);// 打开AddNoteActivity
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //startActivityForResult()的回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_NOTE:
            case REQUEST_CODE_ADD_NOTE:
                //编辑或新增便签信息后，需要重新加载当前页面，因为当前页面的内容可能更新了
                loadCurrentPageData();
                break;
            default:
                break;
        }
    }

    private static class NoteViewHolder {
        public TextView noteIdTextView; // 便签编号
        public TextView noteNoteTextView; // 便签内容
        public CheckBox noteCheckBox;     //是否选中
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
            if (noteBaseAdapter != null) noteBaseAdapter.notifyDataSetChanged();
        }

        /**
         * Called when action mode is first created. The menu supplied will be used to
         * generate action buttons for the action mode.
         */
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // ActionMode的菜单处理
            getMenuInflater().inflate(R.menu.menu_note_browse_context, menu);
            actionMode = mode;
            return true;
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated.
         */
        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            noteSelectAllCheckBox.setVisibility(View.VISIBLE);
            noteBaseAdapter.setCheckBoxVisibility(View.VISIBLE);
            noteBaseAdapter.notifyDataSetChanged();
            mode.setTitle("完成");
            return true;
        }

        /**
         * Called to report a user click on an action button.
         */
        @Override
        public boolean onActionItemClicked(final android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_note_browse_delete:
                    // retrieve selected item and delete it out
                    new AlertDialog.Builder(NoteBrowseActivity.this).setIcon(android.R.drawable.ic_delete)
                            .setMessage("确认删除已选中的便签信息吗？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (noteDao == null)
                                        noteDao = NoteDao.getNoteDaoInstance(NoteBrowseActivity.this);
                                    long[] selectedIds = noteListView.getCheckedItemIds();
                                    if (noteDao.delete(selectedIds) > 0) {
                                        //删除便签信息后，需要重新加载当前页面，因为页面总数和当前页的内容可能删除了
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
            noteListView.clearChoices();
            noteSelectAllCheckBox.setVisibility(View.GONE);
            noteBaseAdapter.setCheckBoxVisibility(View.GONE);
            noteBaseAdapter.notifyDataSetChanged();
            actionMode = null;
        }
    }

    //ListView适配器
    private class NoteBaseAdapter extends BaseAdapter {
        private int checkBoxVisibility = View.GONE;
        //设置CheckBox默认不可见,在ActionMode的onPrepareActionMode 设置可见,在ActionMode的onDestroyActionMode又设置回不可见
        private LayoutInflater mInflater = null;
        private NoteViewHolder noteViewHolder;

        public NoteBaseAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (noteList == null)
                return 0;
            return noteList.size();
        }

        @Override
        public Note getItem(int position) {
            if (noteList == null)
                return null;
            return noteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (noteList == null)
                return 0;
            return noteList.get(position).getId();
        }

        //hasStableIds返回true时，ListView的getCheckedItemIds才有效
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                noteViewHolder = new NoteViewHolder();
                convertView = mInflater.inflate(R.layout.item_listview_note_browse, parent, false);
                noteViewHolder.noteIdTextView = (TextView) convertView
                        .findViewById(R.id.itemNoteIdTextView);
                noteViewHolder.noteNoteTextView = (TextView) convertView
                        .findViewById(R.id.itemNoteNoteTextView);
                noteViewHolder.noteCheckBox = (CheckBox) convertView.findViewById(R.id.itemNoteCheckBox);
                convertView.setTag(noteViewHolder);
            } else {
                noteViewHolder = (NoteViewHolder) convertView.getTag();
            }
            Note note = noteList.get(position);
            noteViewHolder.noteIdTextView.setText(String.valueOf(note.getId()));
            noteViewHolder.noteNoteTextView.setText(note.getNote());
            noteViewHolder.noteCheckBox.setChecked(noteListView.isItemChecked(position));
            noteViewHolder.noteCheckBox.setVisibility(checkBoxVisibility);
            return convertView;
        }

        public void setCheckBoxVisibility(int checkBoxVisibility) {
            this.checkBoxVisibility = checkBoxVisibility;
        }
    }
}
