package com.example.combobackup.table

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.combobackup.R
import com.example.combobackup.table.TableAdapter.ViewHolder

class TableAdapter : RecyclerView.Adapter<ViewHolder>() {

    private var mData: ArrayList<TableItem>? = null

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var no1Text: TextView
        var no2Text: TextView

        var power1Text : TextView
        var power2Text : TextView

        var time1Text : TextView
        var time2Text : TextView

        init {

            // 뷰 객체에 대한 참조. (hold strong reference)
            no1Text = itemView.findViewById(R.id.sNoText)
            no2Text = itemView.findViewById(R.id.sNoText2)

            power1Text = itemView.findViewById(R.id.powerText1)
            power2Text = itemView.findViewById(R.id.powerText2)

            time1Text = itemView.findViewById(R.id.timeText1)
            time2Text = itemView.findViewById(R.id.timeText2)
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    fun setList(list: ArrayList<TableItem>?) {
        mData = list
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val context: Context = parent.getContext()
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.table_item_recyclerview, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: TableAdapter.ViewHolder, position: Int) {
        val text = mData!![position]
        holder.no1Text?.text = text.toString()

    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    override fun getItemCount(): Int {
        return mData!!.size
    }
}