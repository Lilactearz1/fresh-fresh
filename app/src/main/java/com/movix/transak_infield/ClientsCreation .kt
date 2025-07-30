package com.movix.transak_infield

import java.sql.Date

open class ClientsCreation (var id:Int,val name:String,val phone:String,vararg creationDate:Date) {}
class Estimateinfo(val id:Int,var creationDate:String,var dueDate: String,val customerId:Int)

