package com.example.samplecoupon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}

@ConfigurationPropertiesScan
@SpringBootApplication //
class Application
