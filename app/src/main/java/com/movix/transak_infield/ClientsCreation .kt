package com.movix.transak_infield

import android.provider.ContactsContract.CommonDataKinds.Email
import java.sql.Date

open class ClientsCreation (var id:Int,val name:String,val phone:String) {}
class Estimateinfo(val id:Int,var titleINV:String,var creationDate:String,var dueDate: String,val customerId:Int)

