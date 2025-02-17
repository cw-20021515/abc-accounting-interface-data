package com.abc.us.accounting.supports.aws

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class AwsS3Config(
    @Value("\${cloud.aws.s3.region:us-west-2}")
    private val region: String
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }
    @Bean("s3Client")
    fun s3Client(): S3Client {
//        last test : 2024-12-13 15:10
        val accessKeyId = "ASIA2LY4MUSGEGYNKPSD"
        val secretAccessKey = "kyspQ1up4MHVBIWvHyuoNtoRDNsyBFYhZLu2dy8l"
        val sessionToken = "IQoJb3JpZ2luX2VjEBYaDmFwLW5vcnRoZWFzdC0yIkgwRgIhAIhjdQ6+f3rgTvepBsoNj5epe4JB1XfNpkMXmNSn6XpBAiEA9fdnDp//KfVCBiO9Q8XcC6/SwdzB8dHT7xBZvbn+8+wqnAMIz///////////ARABGgw3MTI0ODcyNDkwMzYiDLzZjh9fSkpGb9XdfCrwAke8u5vbk5ZYLLY8+rPIZ+pm5hh1g1nIpZs4swctgKUUZVGrfcJK0c52uhAszG6EyUGMttzDSyqh81f5ERqzjSDfr6yImOtM/qlFXpW5PicuSecoENmBLEFobNbg7xNYrx6PE+/HNpemEXtO/KGzy+D03bWlj4sz+1BLzHV4ey8uI53WTFhJVAdyOWOYH+Xnei8k3Jk1JTQGld256prKnzHppewQs5IW45vJ1rtrva2aeCCPcsf3k7bewevByCJWqSNTOAR0j+VsAp0kjTT7raotNzDNzcWN/xFO60DCa57TmSLvU2Vm/82i2RK/jDY8DaNOEmaobos3YZ5cAbip2onrG5I/DP0JhDOKYKX44DlHH/yEgD0Jak5941CxxOgEj5ZirhUZN09iVR2aunzMw15Q1tJ0AU2BvAXuiXib+xeBpQwsypjPeP4yX4hlWIZ6xCXUbPNbmee6qC7XvEeGEAutSdaMEq2IouqZsnG3X/faMOac77oGOqUBSVmCwy7viutDWvSnq72EYJWIFNAVGZpuA1zIzslC2lgSSivgzvDOIoUcMbK/B5wvOBbL0MKeYPLYmO/k0ejh+GHPmAfVkhEUiL2+L+67r50w1+0kEdzdHu0CDhbS29FwmU1ofNYIEpEsnRlSkGZBw9B2v6wVh9gqkYOuFq1Hp81scz+7pxAEoSZDJIFNgM9S41MBmuov5o15qTHT994KR9D9TjrM"
        val credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
        // S3 클라이언트 생성
        return S3Client.builder()
            .region(Region.of(region)) // 원하는 리전으로 설정
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
        return S3Client.builder()
            .region(Region.of(region)) // 프로파일에 설정된 지역을 사용하거나 직접 지정
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()

    }
}
