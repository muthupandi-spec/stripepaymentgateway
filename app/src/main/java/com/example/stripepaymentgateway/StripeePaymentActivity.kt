package com.example.stripepaymentgateway

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONException
import org.json.JSONObject


class StripeePaymentActivity : AppCompatActivity() {
    var publishkey: String = "enter your pktest or live  key"
    var secretkey: String = "enter your secret text or live key"
    var paymentsheet: PaymentSheet? = null
    var configuration: PaymentSheet.CustomerConfiguration? = null
    lateinit var paymentIntentClientSecret: String
    var customerId: String? = null
    var empericalkey: String? = null
    var clientsecret: String? = null
    var stripeid: String? = null
    var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.strip_layout)
        PaymentConfiguration.init(applicationContext, publishkey)

        paymentsheet = PaymentSheet(this) { paymentSheetResult ->

            paymentresult(paymentSheetResult)

        }
        button = findViewById(R.id.pay)
        button!!.setOnClickListener {
            paymentflow()
        }


        val stringRequest = object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/customers",
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    customerId = jsonObject.getString("id")
                    Toast.makeText(this, customerId, Toast.LENGTH_SHORT).show()
                    getEphericKey(customerId.toString())

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()

                // Handle the error
            }) {

            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + secretkey
                return headers
            }
        }

        val requestQueue = Volley.newRequestQueue(this@StripeePaymentActivity)
        requestQueue.add(stringRequest)

    }

    private fun paymentresult(paymentSheetResult: PaymentSheetResult) {
        if (paymentSheetResult is PaymentSheetResult.Completed) {

            Toast.makeText(this, "payment success", Toast.LENGTH_SHORT).show()

        }
    }


    private fun getEphericKey(customerId: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    empericalkey = jsonObject.getString("id")
                    getclientsecret(customerId.toString(), empericalkey.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()

            }) {

            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + secretkey
                headers["Stripe-version"] = "2022-08-01"
                return headers
            }

            override fun getParams(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("customer", customerId)
                return headers
            }
        }

        val requestQueue = Volley.newRequestQueue(this@StripeePaymentActivity)
        requestQueue.add(stringRequest)


    }

    private fun getclientsecret(customerId: String, empericalkey: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    clientsecret = jsonObject.getString("client_secret")
                    paymentflow()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                // Handle the error
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer " + secretkey)
                headers.put("Stripe-Version", "2023-10-16")
                return headers
            }

            override fun getParams(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("customer", customerId)
                headers.put("amount", "1000" + "00")
                headers.put("currency", "usd")
                headers.put("automatic_payment_methods[enabled]", "true")
                return headers
            }
        }

        val requestQueue = Volley.newRequestQueue(this@StripeePaymentActivity)
        requestQueue.add(stringRequest)
    }

    private fun paymentflow() {
        paymentsheet!!.presentWithPaymentIntent(
            clientsecret.toString(),
            PaymentSheet.Configuration(
                "ABCCompany",
                PaymentSheet.CustomerConfiguration(
                    customerId.toString(),
                    empericalkey.toString()
                )
            )
        )
    }

}