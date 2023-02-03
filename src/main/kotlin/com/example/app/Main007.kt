package com.example.app

import kotlinx.coroutines.*
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.ceil

val BUCKET = "taito0625-delete-me"
val n = 1000

fun main() {
    // createTestData(n) テストデータ作成用
    test("exists1", n, ::exists1)
    test("exists2", n, ::exists2)
    testAsync("exists2Async", n, ::exists2Async)
    testKotlinAsync("exists2KotlinAsync", n, ::exists2KotlinAsync)
    testAsync2("exists2Async2", n, ::exists2Async2)
}

fun createTestData(n: Int) {
    val cli = S3Client.builder()
        .region(Region.AP_NORTHEAST_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()
    (0..n).forEach {
        val req = PutObjectRequest.builder()
            .bucket(BUCKET)
            .key("a${it}.txt")
            .build()
        cli.putObject(req, RequestBody.fromString("hello world!"))
    }
}

fun test(name: String, n: Int, f: (S3Client, Int, MutableList<Long>) -> Unit) {
    val cli = S3Client.builder()
        .region(Region.AP_NORTHEAST_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()
    val latencies = ArrayList<Long>()
    val start = System.currentTimeMillis()
    f(cli, n, latencies)
    val end = System.currentTimeMillis()
    println("%s count=%d total=%d ave:%.3f 90=%d 95=%d 99=%d".format(
        name,
        n,
        end-start,
        ave(latencies),
        percentile(latencies, 90.0),
        percentile(latencies, 95.0),
        percentile(latencies, 99.0)
    ))
    cli.close()
}

fun testAsync(name: String, n: Int, f: (S3AsyncClient, Int, MutableList<Long>, Lock) -> Unit) {
    val cli = S3AsyncClient.builder()
        .region(Region.AP_NORTHEAST_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()
    val latencies = ArrayList<Long>()
    val lock = ReentrantLock()
    runBlocking {
        val start = System.currentTimeMillis()
        f(cli, n, latencies, lock)
        val end = System.currentTimeMillis()
        println(
            "%s count=%d total=%d ave:%.3f 90=%d 95=%d 99=%d".format(
                name,
                n,
                end - start,
                ave(latencies),
                percentile(latencies, 90.0),
                percentile(latencies, 95.0),
                percentile(latencies, 99.0)
            )
        )
    }
    cli.close()
}

fun testKotlinAsync(name: String, n: Int, f: (aws.sdk.kotlin.services.s3.S3Client, Int, MutableList<Long>, Lock) -> Unit) {
    val latencies = ArrayList<Long>()
    val lock = ReentrantLock()
    runBlocking {
        aws.sdk.kotlin.services.s3.S3Client
            .fromEnvironment { region = "ap-northeast-1" }
            .use { cli ->
                val start = System.currentTimeMillis()
                f(cli, n, latencies, lock)
                val end = System.currentTimeMillis()
                println(
                    "%s count=%d total=%d ave:%.3f 90=%d 95=%d 99=%d".format(
                        name,
                        n,
                        end - start,
                        ave(latencies),
                        percentile(latencies, 90.0),
                        percentile(latencies, 95.0),
                        percentile(latencies, 99.0)
                    )
                )
                cli.close()
            }
    }
}

fun testAsync2(name: String, n: Int, f: (S3Client, Int, MutableList<Long>, Lock) -> Unit) {
    val cli = S3Client.builder()
        .region(Region.AP_NORTHEAST_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()
    val latencies = ArrayList<Long>()
    val lock = ReentrantLock()
    val start = System.currentTimeMillis()
    f(cli, n, latencies, lock)
    val end = System.currentTimeMillis()
    println("%s count=%d total=%d ave:%.3f 90=%d 95=%d 99=%d".format(
        name,
        n,
        end-start,
        ave(latencies),
        percentile(latencies, 90.0),
        percentile(latencies, 95.0),
        percentile(latencies, 99.0)
    ))
    cli.close()
}

// exists impl using listObjectV2
fun exists1(cli: S3Client, n: Int, latencies: MutableList<Long>) {
    (0..n).forEach {
        val req = ListObjectsV2Request.builder()
            .bucket(BUCKET)
            .prefix("a${it}.txt")
            .build()
        val start = System.currentTimeMillis()
        val result = cli.listObjectsV2(req)
        result.contents().size
        val end = System.currentTimeMillis()
        latencies.add(end - start)
    }
}

// exists impl using head
fun exists2(cli: S3Client, n: Int, latencies: MutableList<Long>) {
    (0..n).forEach {
        val req = HeadObjectRequest.builder()
            .bucket(BUCKET)
            .key("a${it}.txt")
            .build()
        val start = System.currentTimeMillis()
        cli.headObject(req)
        val end = System.currentTimeMillis()
        latencies.add(end - start)
    }
}

// exists impl using head async
fun exists2Async(cli: S3AsyncClient, n: Int, latencies: MutableList<Long>, lock: Lock) {
    runBlocking {
        val all = (0..n).map {
            async {
                val req = HeadObjectRequest.builder()
                    .bucket(BUCKET)
                    .key("a${it}.txt")
                    .build()
                val start = System.currentTimeMillis()
                cli.headObject(req).await()
                val end = System.currentTimeMillis()
                lock.withLock {
                    latencies.add(end - start)
                }
            }
        }
        all.awaitAll()
    }
}

// exists impl using head async
fun exists2KotlinAsync(cli: aws.sdk.kotlin.services.s3.S3Client, n: Int, latencies: MutableList<Long>, lock: Lock) {
    runBlocking {
        val all = (0..n).map {
            async {
                val start = System.currentTimeMillis()
                cli.headObject(aws.sdk.kotlin.services.s3.model.HeadObjectRequest {
                    bucket = BUCKET
                    key = "a${it}.txt"
                })
                val end = System.currentTimeMillis()
                lock.withLock {
                    latencies.add(end - start)
                }
            }
        }
        all.awaitAll()
    }
}

// exists impl using head
fun exists2Async2(cli: S3Client, n: Int, latencies: MutableList<Long>, lock: Lock) {
    val pool = Executors.newFixedThreadPool(10)
    runBlocking(pool.asCoroutineDispatcher()) {
        (0..n).map {
            async {
                val req = HeadObjectRequest.builder()
                    .bucket(BUCKET)
                    .key("a${it}.txt")
                    .build()
                val start = System.currentTimeMillis()
                cli.headObject(req)
                val end = System.currentTimeMillis()
                lock.withLock {
                    latencies.add(end - start)
                }
            }
        }.awaitAll()
    }
    pool.shutdown()
}

fun percentile(data: List<Long>, p: Double): Long {
    val sorted = data.sorted()
    val i = ceil((p / 100) * sorted.size).toInt()
    return sorted[i-1]
}

fun ave(data: List<Long>): Double {
    var s = 0.0
    data.forEach { s+=it }
    return s / data.size
}

internal suspend fun <T> CompletableFuture<T>.await(): T =
    suspendCoroutine { cont: Continuation<T> ->
        whenComplete { result, exception ->
            if (exception == null) {
                cont.resume(result) // the future has been completed normally
            } else {
                cont.resumeWithException(exception) // the future has completed with an exception
            }
        }
    }
