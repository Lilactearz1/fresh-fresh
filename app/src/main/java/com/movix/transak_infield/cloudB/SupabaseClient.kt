package com.movix.transak_infield.cloudB

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://oamlmyagicaucsaxyanu.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9hbWxteWFnaWNhdWNzYXh5YW51Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTU2OTQsImV4cCI6MjA4ODM3MTY5NH0.f4-TIKjxjouNlGDC80npJF9tnlMPwv7J5JKRVpGcHZQ"
    ) {
        install(io.github.jan.supabase.storage.Storage)
    }
}