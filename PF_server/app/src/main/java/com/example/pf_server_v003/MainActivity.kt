package com.example.pf_server_v003

import android.content.Intent
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.ServerSocket
import android.widget.AdapterView
import android.widget.AdapterView.*
import java.net.Socket


class MainActivity : AppCompatActivity() ,OnItemSelectedListener{
    private lateinit var imgArrow : ImageView
    private lateinit var btnStart : Button
    private lateinit var tvMsg : TextView
    private lateinit var tvServerStat : TextView

    private lateinit var serHandler: Handler
    private lateinit var mainHandler: Handler
    private lateinit var serverHT : HandlerThread

    private lateinit var spin001 : Spinner

    private lateinit var jData : JSONObject
    private var serverIsRunning : Boolean = false

    private enum class MSGtype{ SERV_ON,SERV_OFF,SERV_BREAK_LOOP,SERV_IMG_ROT};
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgArrow=findViewById(R.id.imgArrow)
        btnStart=findViewById(R.id.btnStart)
        tvMsg=findViewById(R.id.tvMsg)
        tvServerStat = findViewById(R.id.tvServerStat)
        spin001=findViewById(R.id.spin_loc)


        serverHT=HandlerThread("serverx")
        serverHT.start()
        serHandler = Handler(serverHT.looper)
        mainHandler = Handler(Looper.getMainLooper(),Handler.Callback {
            //here setting what should mainThread do according to the Message obejct
            //"it" is a instance of Message()
            when(it.what){
                MSGtype.SERV_ON.ordinal         -> {
                    Log.d("serverx", "running a server")
                    tvServerStat.setText("running a server")
                }
                MSGtype.SERV_BREAK_LOOP.ordinal -> {
                    Log.d("serverx", "close a server(break loop)")
                    tvServerStat.setText("close a server(break loop)")
                }
                MSGtype.SERV_IMG_ROT.ordinal    -> {
                    Log.d("serverx", jData.toString())
                    //Log.d("selectt", spin001.selectedItem.toString())
                    //Toast.makeText(this,spin001.selectedItem.toString(),Toast.LENGTH_SHORT).show()
                    //==
                    var way = jData.get("Way").toString()
                    when(way){
                        "North" -> imgArrow.rotation=0.0f
                        "East" -> imgArrow.rotation=90.0f
                        "South" -> imgArrow.rotation=180.0f
                        "West" -> imgArrow.rotation=270.0f
                        else -> {
                            try{
                                imgArrow.rotation = way.toFloat()
                            }
                            catch (e : Exception){
                                tvMsg.setText("parseFloat ERROR")
                            }
                        }
                    }
                    //==


                    tvMsg.setText(jData.toString())
                }
                MSGtype.SERV_OFF.ordinal        -> tvServerStat.setText("close a server")
            }
            false
        })


        var adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,R.array.loc_array,android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spin001.adapter = adapter
        spin001.onItemSelectedListener = this


        btnStart.setOnClickListener {
            if (serverIsRunning) {
                serverIsRunning = false
                mainHandler.sendMessage(
                    // a anonymous instance initialize using ".also"
                    Message().also { it.what = MSGtype.SERV_BREAK_LOOP.ordinal }
                )
            } else {
                serHandler.post {
                    try {
                        val server = ServerSocket(5566)
                        mainHandler.sendMessage(
                            Message().also { it.what = MSGtype.SERV_ON.ordinal }
                        )

                        serverIsRunning = true
                        while (serverIsRunning) {
                            val client = server.accept()
                            val output = PrintWriter(client.getOutputStream(), true)
                            val input = BufferedReader(InputStreamReader(client.getInputStream()))

                            jData = JSONObject(input.readLine())
                            output.println("server got the jXon str")      //reply

                            mainHandler.sendMessage(
                                Message().also { it.what = MSGtype.SERV_IMG_ROT.ordinal }
                            )

                            input.close()
                            output.close()
                        }

                        server.close()
                        mainHandler.sendMessage(
                            Message().also { it.what = MSGtype.SERV_OFF.ordinal }
                        )
                    } catch (e: Exception) {
                        Log.d("serverx", e.toString())
                        this@MainActivity.runOnUiThread {
                            tvServerStat.setText(e.toString())
                        }
                    }
                }
            }

        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        var text : String = p0!!.getItemAtPosition(p2).toString()
        Toast.makeText(p0.context,text,Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}