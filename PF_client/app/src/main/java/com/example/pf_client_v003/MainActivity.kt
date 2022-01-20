package com.example.pf_client_v003

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var btnSet : Button
    private lateinit var btnSend : Button
    private lateinit var etIPAddr : EditText
    private lateinit var etMsg : EditText

    private lateinit var cliHandler: Handler
    private lateinit var mainHandler: Handler
    private lateinit var clientHT : HandlerThread


    private lateinit var msg : String
    private var jData = JSONObject()
    private var serverIPaddr = "192.168.0.100"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSet=findViewById(R.id.btnSet)
        btnSend=findViewById(R.id.btnSend)
        etIPAddr=findViewById(R.id.etIPAddr)
        etMsg=findViewById(R.id.etMsg)

        clientHT = HandlerThread("clientx")
        clientHT.start()
        cliHandler= Handler(clientHT.looper)
        mainHandler = Handler(Looper.getMainLooper())

        btnSet.setOnClickListener {
            serverIPaddr=etIPAddr.text.toString()
            Toast.makeText(this,"Set server IP:${serverIPaddr}",Toast.LENGTH_SHORT).show()
        }

        btnSend.setOnClickListener {
            cliHandler.post {
                try{
                    Log.d("clientx","create client")
                    val client = Socket(serverIPaddr,5566)
                    val output = PrintWriter(client.getOutputStream(),true)
                    val input = BufferedReader(InputStreamReader(client.getInputStream()))

                    var str ="init"
                    mainHandler.post {
                        str = etMsg.text.toString()
                    }
                    Thread.sleep(10)                    //here will occur race condition
                    jData.put("Way",str)
                    output.println(jData.toString())
                    str=input.readLine()
                    Log.d("clientx",str)            //get reply
                    mainHandler.post {
                        Toast.makeText(this@MainActivity,str,Toast.LENGTH_SHORT).show()
                    }

                    client.close()
                }
                catch (e:Exception) {
                    Log.d("clientx",e.toString())
                    mainHandler.post {
                        Toast.makeText(this@MainActivity,e.toString(),Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

    }
}