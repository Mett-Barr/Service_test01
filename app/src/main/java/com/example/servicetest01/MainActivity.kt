package com.example.servicetest01

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.servicetest01.databinding.ActivityMainBinding
import kotlin.coroutines.coroutineContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    /** ----------------------Binder--------------------------------  */
    private lateinit var binderService: LocalService
    private var binderBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val binderConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalService.LocalBinder
            binderService = binder.getService()
            binderBound = true

            Log.d("TAG", "onServiceConnected: ")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            binderBound = false
        }
    }
    /**---------------------------Binder----------------------------*/


    /**--------------------------Messenger--------------------------*/
    /** Messenger for communicating with the service.  */
    private var messengerService: Messenger? = null

    /** Flag indicating whether we have called bind on the service.  */
    private var messengerBound: Boolean = false

    /**
     * Class for interacting with the main interface of the service.
     */
    private val messengerConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            /**
             * 4.客戶端使用IBinder(service)將Messenger（引用服務的Handler）實例化，
             * 然後使用Messenger將Message 對象發送給服務(messengerService)
             */
            messengerService = Messenger(service)
            messengerBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            messengerService = null
            messengerBound = false
        }
    }

    /** 用於接收服務返回的消息 */
    private var mReceiverReplyMsg: Messenger = Messenger(ReceiverReplyMsgHandler(this))

    internal class ReceiverReplyMsgHandler
        (
        context: Context,
        private val applicationContext: Context = context
    )
        :
        Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SAY_HELLO ->
                    Toast.makeText(applicationContext, "Receiver Work!", Toast.LENGTH_SHORT)
                        .show()
//                    Log.d("TAG", "handleMessage: ")

                else -> super.handleMessage(msg)
            }
        }
    }

    private fun sayHello(v: View) {
        if (!messengerBound) return
        // 創建與服務交互的消息實體Message
        // Create and send a message to the service, using a supported 'what' value
        val msg: Message = Message.obtain(null, MSG_SAY_HELLO, 0, 0)
        // 把接受服務端回復的Messenger通過Message的replyTo參數傳遞給服務端
        msg.replyTo = mReceiverReplyMsg
        try {
            messengerService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**--------------------------Messenger--------------------------*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        init()
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, LocalService::class.java).also { intent ->
            bindService(intent, binderConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(this, MessengerService::class.java).also { intent ->
            bindService(intent, messengerConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        unbindService(binderConnection)
        binderBound = false

        if (messengerBound) {
            unbindService(messengerConnection)
            messengerBound = false
        }
    }


    private fun init() {
        serviceInit()
        binderInit()
        messengerInit()
    }

    private fun serviceInit() {
        var boo = true
        val intent = Intent(this, TestService::class.java)
        binding.service.setOnClickListener {
            boo = if (boo) {
                startService(intent)
                !boo
            } else {
                stopService(intent)
                !boo
            }
        }
    }

    private fun binderInit() {
        binding.binder.setOnClickListener {
            if (binderBound) {
                // Call a method from the LocalService.
                // However, if this call were something that might hang, then this request should
                // occur in a separate thread to avoid slowing down the activity performance.
                val num: Int = binderService.randomNumber
                Toast.makeText(this, "number: $num", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun messengerInit() {
        binding.messager.setOnClickListener {
            sayHello(it)
        }
    }


}


