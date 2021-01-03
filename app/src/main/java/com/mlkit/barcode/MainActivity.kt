package com.mlkit.barcode

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mlkit.barcode.databinding.ActivityMainBinding
import com.mlkit.barcode.ui.BarcodeActivity


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hookViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK){
                val message: String? = data?.getStringExtra("MESSAGE")
                Toast.makeText(this, message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hookViews() {
        binding.mbtnBarcode.setOnClickListener {
            startActivityForResult(
                Intent(this@MainActivity, BarcodeActivity::class.java),
                REQUEST_CODE
            )
        }
    }


    companion object{
        const val REQUEST_CODE = 101;
    }


}