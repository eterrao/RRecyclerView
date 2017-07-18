package tech.shutu.rrecyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.welove520.rrefresh.view.base.IPtrViewFactory;
import com.welove520.rrefresh.view.base.RRefreshView;
import com.welove520.rrefresh.view.base.WeloveHeader;
import com.welove520.rrefresh.view.listener.OnLoadMoreListener;
import com.welove520.rrefresh.view.listener.OnRefreshListener;
import com.welove520.rrefresh.view.network.StatusLayout;
import com.welove520.rrefresh.view.recyclerview.RecyclerAdapterWithHF;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static final int REFRESH_DELAY = 2000;
    public static final String KEY_ICON = "icon";

    public static final String KEY_COLOR = "color";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.pull_to_refresh)
    RRefreshView mPullToRefreshView;
//    @BindView(R.id.lv_main)
//    ListView lvMain;

    private RecyclerAdapter adapter;
    private RecyclerAdapterWithHF mAdapter;

    private ListViewAdapter mListAdapter;
    private List<String> mData = new ArrayList<String>();
    private int page = 0;
    private int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerAdapter(this, mData);
        mAdapter = new RecyclerAdapterWithHF(adapter);
        mListAdapter = new ListViewAdapter(mData);
        recyclerView.setAdapter(mAdapter);
        adapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (counter++ % 2 == 0) {
                    mPullToRefreshView.showNetwork(StatusLayout.NetworkStatusLayout.STATUS_RETRY);
                } else {
                    mPullToRefreshView.showNetwork(StatusLayout.NetworkStatusLayout.STATUS_NO_NET);
                }
            }
        });
        mPullToRefreshView.setHeaderView(new WeloveHeader());

//        lvMain.setAdapter(mListAdapter);
//        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this, "position clicked : " + position, Toast.LENGTH_SHORT).show();
//            }
//        });
        mPullToRefreshView.setAutoLoadMoreEnable(true);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mData.clear();
                        for (int i = 0; i < 17; i++) {
                            mData.add(new String("item  -" + i));
                        }

                        mAdapter.notifyDataSetChanged();
                        mListAdapter.notifyDataSetChanged();
                        mPullToRefreshView.setRefreshing(false);
                        mPullToRefreshView.setLoadMoreEnable(true);
                        mPullToRefreshView.showData(StatusLayout.DataStatusLayout.STATUS_EMPTY);
                    }
                }, REFRESH_DELAY);
            }
        });

        mPullToRefreshView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mData.add(new String("  RecyclerView item  - add " + page));
                        mAdapter.notifyDataSetChanged();
                        mListAdapter.notifyDataSetChanged();
                        mPullToRefreshView.loadMoreComplete(true);
                        page++;
                        Toast.makeText(MainActivity.this, "load more complete", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }


    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        View.OnClickListener listener;
        private List<String> datas;
        private LayoutInflater inflater;

        public RecyclerAdapter(Context context, List<String> data) {
            super();
            inflater = LayoutInflater.from(context);
            datas = data;
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ChildViewHolder holder = (ChildViewHolder) viewHolder;
            holder.itemTv.setText(datas.get(position));
            holder.itemTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(v);
                    }
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewHolder, int position) {
            View view = inflater.inflate(R.layout.listitem_layout, null);
            return new ChildViewHolder(view);
        }

        public void setListener(View.OnClickListener listener) {
            this.listener = listener;
        }
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTv;

        public ChildViewHolder(View view) {
            super(view);
            itemTv = (TextView) view;
        }

    }

    public static class ListViewAdapter extends BaseAdapter {
        private List<String> list;

        public ListViewAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return (list != null) ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return (list != null) ? list.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_layout, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (list != null && list.size() > 0) {
                holder.text1.setText(list.get(position));
            }
            return convertView;
        }

        static class ViewHolder {
            @BindView(android.R.id.text1)
            TextView text1;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
