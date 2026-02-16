package com.progbe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ProgBeApplication

fun main(args: Array<String>) {
    runApplication<ProgBeApplication>(*args)
}
