/*
 * Copyright 2018 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.springbootbuch.raffles.retweet

import jp.nephy.penicillin.PenicillinClient
import okhttp3.ConnectionPool
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@SpringBootApplication
@EnableConfigurationProperties(RaffleByRetweetProperties::class)
class RaffleByRetweetApplication

@ConfigurationProperties(prefix = "raffle-by-retweet")
class RaffleByRetweetProperties {
    lateinit var consumerKey: String;
    lateinit var consumerSecret: String;
    lateinit var accessToken: String;
    lateinit var accessTokenSecret: String;
    var sourceTweetId: Long = 0;
}

@Component
class RunRaffle(val config: RaffleByRetweetProperties) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val client = PenicillinClient.build {
            application(config.consumerKey, config.consumerSecret)
            token(config.accessToken, config.accessTokenSecret)
            httpClient {
                connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            }
        }

        val retweets = client.status
                .retweets(id = config.sourceTweetId, count = 512)
                .complete().result
        val winner = retweets[ThreadLocalRandom.current().nextInt(retweets.size)]
        println("${winner.user.screenName} (${winner.user.name})")
    }
}

fun main(args: Array<String>) {
    runApplication<RaffleByRetweetApplication>(*args)
}
