package MuYuan.RecyclerViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.qiuchen.myapplication.R;

import java.util.List;
import java.util.Map;

/**
 * Auther: cheny
 * CreateDate 6/25/2017.
 */

public class mAdapterByShortHolidays extends RecyclerView.Adapter<mAdapterByShortHolidays.mHoldlerView> {
    private List<Map<String, String>> mList;
    private Context mContext;

    public void setAdapterData(List<Map<String, String>> list, Context context) {
        mList = list;
        mContext = context;
    }

    //返回视图
    @Override
    public mHoldlerView onCreateViewHolder(ViewGroup parent, int viewType) {
        return new mHoldlerView(LayoutInflater.from(mContext).inflate(R.layout.listviewadapteritem, parent, false));
    }

    //业务逻辑处理
    @Override
    public void onBindViewHolder(mHoldlerView holder, int position) {
        Map<String, String> m = mList.get(position);
        holder.mItemIndex.setText(m.get("mItemIndex"));
        holder.mItem_HolidayBecause.setText(m.get("mItem_HolidayBecause"));
        holder.mItem_HolidayTime.setText(m.get("mItem_HolidayTime"));
        holder.mItem_WhereOutSide.setText(m.get("mItem_WhereOutSide"));
        holder.mItemAcceptState.setText(m.get("mItemAcceptState"));
        switch (m.get("mItemAcceptState")){
            case "通过":
                holder.mAdapterViewBackGround.setCardBackgroundColor(Color.argb(150,255,255,255));
                break;
            case "不通过":
                holder.mAdapterViewBackGround.setCardBackgroundColor(Color.argb(200,223,67,0));
                break;
            case "等待处理":
                holder.mAdapterViewBackGround.setCardBackgroundColor(Color.argb(200,223,67,0));
                break;
            case "退回":
                holder.mAdapterViewBackGround.setCardBackgroundColor(Color.argb(200,223,67,0));
                break;
            default:
                holder.mAdapterViewBackGround.setCardBackgroundColor(Color.argb(150,255,255,255));
                break;
        }


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class mHoldlerView extends RecyclerView.ViewHolder {
        TextView mItemIndex;//项目标号
        TextView mItem_HolidayBecause;//请假原因
        TextView mItem_HolidayTime;//请假时间
        TextView mItem_WhereOutSide;//外出地址
        TextView mItemAcceptState;//老师处理意见
        CardView mAdapterViewBackGround;

        mHoldlerView(View itemView) {
            super(itemView);
            //绑定控件
            mItemIndex = (TextView) itemView.findViewById(R.id.mItemIndex);
            mItem_HolidayBecause = (TextView) itemView.findViewById(R.id.mItem_HolidayBecause);
            mItem_HolidayTime = (TextView) itemView.findViewById(R.id.mItem_HolidayTime);
            mItem_WhereOutSide = (TextView) itemView.findViewById(R.id.mItem_WhereOutSide);
            mItemAcceptState = (TextView) itemView.findViewById(R.id.mItemAcceptState);
            mAdapterViewBackGround = (CardView) itemView.findViewById(R.id.mAdapterViewBackGround);
        }
    }
}
