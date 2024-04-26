package com.bnyro.contacts

import com.bnyro.contacts.util.CalendarUtils
import com.bnyro.contacts.util.SmsUtil
import com.bnyro.contacts.util.receivers.SmsReceiver
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun dateConversion() {
        val date = "2023-05-09"
        val millis = CalendarUtils.dateToMillis(date)!!
        assertEquals(date, CalendarUtils.millisToDate(millis, CalendarUtils.isoDateFormat))
    }

    @Test
    fun splitLongSms() {
        val text =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ornare lectus sit amet est placerat in egestas. Lectus sit amet est placerat. Molestie a iaculis at erat pellentesque adipiscing commodo elit. Sociis natoque penatibus et magnis dis parturient montes. Non enim praesent elementum facilisis leo vel fringilla est. Ut morbi tincidunt augue interdum velit euismod. Pharetra massa massa ultricies mi quis hendrerit. At erat pellentesque adipiscing commodo elit at imperdiet. Risus commodo viverra maecenas accumsan lacus vel facilisis volutpat est. In hac habitasse platea dictumst quisque sagittis purus. Netus et malesuada fames ac turpis egestas sed tempus. Neque volutpat ac tincidunt vitae semper quis lectus. Dui faucibus in ornare quam viverra.\n" +
                "\n" +
                "Tempor id eu nisl nunc mi ipsum. Nulla porttitor massa id neque aliquam vestibulum morbi. Sit amet nulla facilisi morbi tempus iaculis urna id. Aliquet bibendum enim facilisis gravida. Velit scelerisque in dictum non consectetur a erat. Sit amet facilisis magna etiam tempor orci eu lobortis. Vulputate enim nulla aliquet porttitor lacus luctus. Ipsum nunc aliquet bibendum enim facilisis gravida neque convallis. Luctus accumsan tortor posuere ac ut consequat semper viverra. Cras adipiscing enim eu turpis egestas pretium aenean pharetra magna. Tristique sollicitudin nibh sit amet commodo nulla facilisi nullam vehicula. Montes nascetur ridiculus mus mauris vitae ultricies leo integer malesuada. Nulla facilisi nullam vehicula ipsum. Turpis egestas sed tempus urna et. Leo integer malesuada nunc vel. Aliquet nec ullamcorper sit amet risus nullam eget felis. Consequat nisl vel pretium lectus quam id leo. Magnis dis parturient montes nascetur ridiculus mus mauris vitae ultricies.\n" +
                "\n" +
                "In nisl nisi scelerisque eu ultrices vitae auctor eu. Eu nisl nunc mi ipsum faucibus vitae aliquet nec ullamcorper. Dictum varius duis at consectetur lorem donec massa. Purus in mollis nunc sed id semper. Euismod nisi porta lorem mollis aliquam ut. Aliquam eleifend mi in nulla posuere sollicitudin aliquam ultrices sagittis. Volutpat maecenas volutpat blandit aliquam etiam erat. Velit laoreet id donec ultrices tincidunt arcu non sodales neque. Vel elit scelerisque mauris pellentesque pulvinar pellentesque habitant morbi. Viverra mauris in aliquam sem fringilla ut morbi tincidunt augue.\n" +
                "\n" +
                "Viverra orci sagittis eu volutpat odio facilisis mauris. Mi eget mauris pharetra et ultrices neque ornare aenean euismod. Facilisis leo vel fringilla est. Vulputate odio ut enim blandit volutpat maecenas. Volutpat commodo sed egestas egestas fringilla phasellus faucibus scelerisque eleifend. Risus at ultrices mi tempus imperdiet nulla. Praesent elementum facilisis leo vel. Eros in cursus turpis massa tincidunt dui ut. Elit ullamcorper dignissim cras tincidunt lobortis feugiat vivamus. Nulla aliquet porttitor lacus luctus accumsan tortor posuere. Vestibulum rhoncus est pellentesque elit ullamcorper dignissim cras tincidunt lobortis. Quis blandit turpis cursus in hac habitasse platea dictumst. Id aliquet risus feugiat in. Condimentum mattis pellentesque id nibh tortor id. Ac tortor dignissim convallis aenean et tortor at risus viverra. Vehicula ipsum a arcu cursus vitae congue mauris rhoncus.\n" +
                "\n" +
                "Porttitor rhoncus dolor purus non. Vel elit scelerisque mauris pellentesque. Semper auctor neque vitae tempus quam. Elementum nibh tellus molestie nunc non blandit massa enim nec. Dictum non consectetur a erat nam at lectus. Vestibulum sed arcu non odio euismod lacinia. Semper auctor neque vitae tempus quam pellentesque nec nam aliquam. Massa placerat duis ultricies lacus sed turpis. Arcu non odio euismod lacinia at quis risus sed vulputate. Mauris in aliquam sem fringilla. Odio tempor orci dapibus ultrices in iaculis. Nec ultrices dui sapien eget mi proin. Nisi quis eleifend quam adipiscing. Morbi tristique senectus et netus et. Neque laoreet suspendisse interdum consectetur.\n" +
                "\n" +
                "Nunc lobortis mattis aliquam faucibus purus in. Augue eget arcu dictum varius duis. Et egestas quis ipsum suspendisse ultrices gravida dictum fusce. Sodales ut etiam sit amet nisl purus. Viverra orci sagittis eu volutpat odio facilisis mauris sit. Turpis cursus in hac habitasse platea. Aenean euismod elementum nisi quis eleifend. Sagittis id consectetur purus ut faucibus pulvinar elementum integer. Turpis egestas pretium aenean pharetra magna ac placerat vestibulum. Et malesuada fames ac turpis egestas sed. Velit egestas dui id ornare arcu odio ut sem. Ut enim blandit volutpat maecenas. Et netus et malesuada fames ac turpis egestas. Cursus eget nunc scelerisque viverra. Eros donec ac odio tempor. Suscipit tellus mauris a diam."

        SmsUtil.splitSmsText(text).forEach {
            assert(it.length <= SmsUtil.MAX_CHAR_LIMIT)
        }
    }

    @Test
    fun matchVerificationCodeRegex() {
        val test1 = "Hello World,123456. That's it."
        val test2 = "Hello World, 123456 . That's it."
        val test3 = "Hello World, 23456 . That's it."

        assertEquals("123456", SmsReceiver.verificationCodeRegex.find(test1)?.value)
        assertEquals("123456", SmsReceiver.verificationCodeRegex.find(test2)?.value)
        assertEquals(null, SmsReceiver.verificationCodeRegex.find(test3)?.value)
    }
}
