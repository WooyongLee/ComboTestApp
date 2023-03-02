package com.example.combobackup

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.combobackup.fragment.ChartFragment

class SaActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sa_main)

//        val fragmentManager = getFragmentManager()
//        val fragmentTransaction = fragmentManager.beginTransaction()

        // FragmentTransaction을 Activity에서 가져오고, Frgment를 추가, 이를 삽입할 뷰를 지정, commit을 통해 변경내용 적용
         val fragment1 = ChartFragment()
         supportFragmentManager.beginTransaction().add(R.id.chartFragment, fragment1).commit()
    }
}