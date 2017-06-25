package MuYuan.RecyclerViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.qiuchen.myapplication.R;

import java.util.List;
import java.util.Map;

/**
 * Auther: cheny
 * CreateDate 6/25/2017.
 */

public class mAdapterByLongHolidaysInfo extends RecyclerView.Adapter<mAdapterByLongHolidaysInfo.mViewHolder> {
    List<Map<String, String>> mList;
    Context context;

    public void setmList(List<Map<String, String>> list, Context contexts) {
        context = contexts;
        mList = list;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mViewHolder m = new mViewHolder(LayoutInflater.from(context).inflate(R.layout.listviewadapteritem, parent,false));
        return m;
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, int position) {
        Map<String, String> m = mList.get(position);
        holder.mItemIndex.setText(m.get("mItemIndex"));
        holder.mItem_HolidayBecause.setText(m.get("mItem_HolidayBecause"));
        holder.mItem_HolidayTime.setText(m.get("mItem_HolidayTime"));
        holder.mItem_WhereOutSide.setText(m.get("mItem_WhereOutSide"));
        holder.mItemAcceptState.setText(m.get("mItemAcceptState"));
        holder.mAdapterViewBackGround.setCardBackgroundColor(Color.argb(170,255,255,255));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class mViewHolder extends RecyclerView.ViewHolder {
        TextView mItemIndex;//项目标号
        TextView mItem_HolidayBecause;//请假原因
        TextView mItem_HolidayTime;//请假时间
        TextView mItem_WhereOutSide;//外出地址
        TextView mItemAcceptState;//老师处理意见
        CardView mAdapterViewBackGround;
        public mViewHolder(View itemView) {
            super(itemView);
            //绑定控件
            mAdapterViewBackGround = (CardView) itemView.findViewById(R.id.mAdapterViewBackGround);
            mItemIndex = (TextView) itemView.findViewById(R.id.mItemIndex);
            mItem_HolidayBecause = (TextView) itemView.findViewById(R.id.mItem_HolidayBecause);
            mItem_HolidayTime = (TextView) itemView.findViewById(R.id.mItem_HolidayTime);
            mItem_WhereOutSide = (TextView) itemView.findViewById(R.id.mItem_WhereOutSide);
            mItemAcceptState = (TextView) itemView.findViewById(R.id.mItemAcceptState);
        }
    }
}
