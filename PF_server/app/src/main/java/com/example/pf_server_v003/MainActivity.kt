package com.example.pf_server_v003

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.*
import android.support.v4.content.ContextCompat
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
import java.lang.Math.floor
import java.net.Socket

class MainActivity : AppCompatActivity() ,OnItemSelectedListener{
    private lateinit var imgArrow : ImageView
    private lateinit var img_nchumap : ImageView
    private lateinit var btnStart : Button
    private lateinit var tvMsg : TextView
    private lateinit var tvServerStat : TextView

    private lateinit var serHandler: Handler
    private lateinit var mainHandler: Handler
    private lateinit var serverHT : HandlerThread

    private lateinit var spin001 : Spinner

    private lateinit var graph : Graph<String?>
    private lateinit var node1  : Vertex<String?>
    private lateinit var node2  : Vertex<String?>
    private lateinit var node3  : Vertex<String?>
    private lateinit var node4  : Vertex<String?>
    private lateinit var node5  : Vertex<String?>
    private lateinit var node6  : Vertex<String?>
    private lateinit var node7  : Vertex<String?>
    private lateinit var node8  : Vertex<String?>
    private lateinit var corner1 : Vertex<String?>
    private lateinit var corner2 : Vertex<String?>
    private lateinit var corner3 : Vertex<String?>
    private lateinit var corner4 : Vertex<String?>
    private lateinit var corner5 : Vertex<String?>
    private lateinit var corner6 : Vertex<String?>
    private lateinit var corner7 : Vertex<String?>
    private lateinit var corner8 : Vertex<String?>
    private lateinit var corner9 : Vertex<String?>

    private lateinit var jData : JSONObject
    private var serverIsRunning : Boolean = false

    private enum class MSGtype{ SERV_ON,SERV_OFF,SERV_BREAK_LOOP,SERV_IMG_ROT};
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgArrow=findViewById(R.id.imgArrow)
        img_nchumap=findViewById(R.id.img_nchumap)
        btnStart=findViewById(R.id.btnStart)
        tvMsg=findViewById(R.id.tvMsg)
        tvServerStat = findViewById(R.id.tvServerStat)
        spin001=findViewById(R.id.spin_loc)

        initMyMap()

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
                    //====
                    /*var way = jData.get("Way").toString()
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
                    }*/
                    //====
                    var dstname = jData.get("Way").toString()
                    var dst : Vertex<String?> = mapName(dstname)
                    var src : Vertex<String?> = mapName(spin001.selectedItem.toString())

                    try {
                        imgArrow.rotation = graph.calTwoNodeAngle(src, dst)!![0].toFloat()
                    }
                    catch (e : Exception){
                        tvMsg.setText("parseFloat ERROR")
                    }


                    var path : String = ""
                    for (v in graph.shortestpath(src,dst)!!.reversed()){
                        path+=v.data+" -> "
                    }
                    tvMsg.setText(path)
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

    fun mapName(s:String) : Vertex<String?>{
        when(s){
            "行政大樓"->return node1
            "惠蓀堂" ->return node2
            "圖書館"->return node3
            "中興湖"->return node4
            "法政行銷大樓"->return node5
            "綜合大樓"->return node6
            "應用科技大樓"->return node7
            "理學大樓"->return node8
            "corner1"->return corner1
            "corner2"->return corner2
            "corner3"->return corner3
            "corner4"->return corner4
            "corner5"->return corner5
            "corner6"->return corner6
            "corner7"->return corner7
            "corner8"->return corner8
            "corner9"->return corner9
        }
        return node1
    }


    fun initMyMap(){
        graph = Graph<String?>()

        node1 = graph.createVertex("行政大樓",listOf(-0.5,1.0))
        node2 = graph.createVertex("惠蓀堂",listOf(0.0,1.5))
        node3 = graph.createVertex("圖書館",listOf(-0.5,-1.0))
        node4 = graph.createVertex("中興湖",listOf(-0.5,0.0))
        node5 = graph.createVertex("法政行銷大樓",listOf(-1.0,-0.5))
        node6 = graph.createVertex("綜合大樓",listOf(-1.5,0.0))
        node7 = graph.createVertex("應用科技大樓",listOf(0.7,0.0))
        node8 = graph.createVertex("理學大樓",listOf(1.5,0.0))
        corner1 = graph.createVertex("corner1",listOf(0.0,1.0))
        corner2 = graph.createVertex("corner2",listOf(1.0,1.0))
        corner3 = graph.createVertex("corner3",listOf(1.0,0.0))
        corner4 = graph.createVertex("corner4",listOf(1.0,-1.0))
        corner5 = graph.createVertex("corner5",listOf(0.0,-1.0))
        corner6 = graph.createVertex("corner6",listOf(-1.0,-1.0))
        corner7 = graph.createVertex("corner7",listOf(-1.0,0.0))
        corner8 = graph.createVertex("corner8",listOf(-1.0,1.0))
        corner9 = graph.createVertex("corner9",listOf(0.0,0.0))

        graph.addNeighbor()
        Log.d("showMap",graph.toString())
        /*
        for(v in graph.shortestpath(node1,node3)!!.reversed() ){
            Log.d("vertx",v.data.toString())
        }
        for(v in graph.calTwoNodeAngle(node1,node3)!!){
            Log.d("vertx",v.toString())
        }
        */

        /*
        var map : Drawable? = ContextCompat.getDrawable(this,R.drawable.maps)
        var reddot : Drawable? = ContextCompat.getDrawable(this,R.drawable.red_dot)
        var naviMap : LayerDrawable = LayerDrawable(arrayOf<Drawable?>(map,reddot))
        naviMap.setLayerWidth(1,200)
        naviMap.setLayerHeight(1,200)
        naviMap.setLayerInset(1,1000,1000,0,0)
        img_nchumap.setImageDrawable(naviMap)
        */

        var map : Drawable? = ContextCompat.getDrawable(this,R.drawable.maps)
        var naviMap : LayerDrawable = LayerDrawable(arrayOf<Drawable?>(map))
        var lnum : Int = 1
        for (v in graph.Nodelist){
            naviMap.addLayer(ContextCompat.getDrawable(this,R.drawable.red_dot))
            naviMap.setLayerWidth(lnum,100)
            naviMap.setLayerHeight(lnum,100)
            naviMap.setLayerInset(lnum, floor(v.coordinate[0]).toInt()*400+1150,floor(v.coordinate[1]).toInt()*370+850,0,0)
            lnum++
        }
        for (v in graph.cornerlist){
            naviMap.addLayer(ContextCompat.getDrawable(this,R.drawable.red_dot))
            naviMap.setLayerWidth(lnum,60)
            naviMap.setLayerHeight(lnum,60)
            naviMap.setLayerInset(lnum, floor(v.coordinate[0]).toInt()*420+1000,floor(v.coordinate[1]).toInt()*350+850,0,0)
            lnum++
        }

        img_nchumap.setImageDrawable(naviMap)
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        var text : String = p0!!.getItemAtPosition(p2).toString()
        Toast.makeText(p0.context,text,Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}