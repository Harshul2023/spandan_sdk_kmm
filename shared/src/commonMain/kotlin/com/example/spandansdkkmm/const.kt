package com.example.spandansdkkmm

object Const {
    const val ERROR_TEST_NOT_VALID = 1
    const val TEST_CANCELED_BY_USER = 2
    const val ERROR_DEVICE_DISCONNECTED = 3

    //Analytics events
    const val SDK_INITIALISE_COMPLETE = "sdk_initialise_complete"
    const val SDK_INITIALISE_FAILED = "sdk_initialise_failed"
    const val DEVICE_CONNECTED = "device_connected"
    const val DEVICE_DISCONNECTED = "device_disconnected"
    const val DEVICE_CONNECTION_TIMEOUT = "device_connection_timeout"
    const val DEVICE_VERIFIED = "device_verified"
    const val TEST_CREATED = "test_created"
    const val TEST_CREATE_FAILED = "test_create_failed"
    const val TEST_STARTED = "test_started"
    const val TEST_FAILED = "test_failed"

    const val TEST_START_CALLED = "test_start_called"

    const val POSITION_RECORDING_COMPLETE = "position_recording_complete"
    const val RECORDING_STARTED = "recording_started"
    const val CANCEL_TEST = "cancel_test"
    const val GENERATE_REPORT_SUCCESS = "generate_report_success"
    const val GENERATE_REPORT_FAILED = "generate_report_failed"
    const val GENERATE_REPORT_CALLED = "generate_report_called"

    //Analytics keys
    const val MASTER_KEY = "master_key"
    const val REASON = "reason"
    const val CONNECTED_DEVICE_TYPE = "device_variant"
    const val TEST_TYPE = "test_type"
    const val POSITION = "position"


//    sdk_initialise_complete || When SDK is initialised || master_key
//    sdk_initialise_failed || When SDK initialisation failed || master_key, reason
//    device_verified || When device is verified || master_key, device_type
//    device_connected || When device is connected || master_key, device_type
//    device_disconnected || When device is disconnected || master_key, device_type
//    device_connection_timeout || When connection timed out || master_key, device_type
//    test_created || When a test is created || master_key, device_type, test_type

//    test_started || When a test is actually started || master_key, device_type, test_type
//    test_failed || When a test fails || master_key, device_type, test_type, reason
//    start_test || When user calls the start test method || master_key, device_type, test_type

//    position_recording_complete || When recording completes for a position || master_key, device_type, test_type, position

//    cancel_test || When a test is cancelled || master_key, device_type, test_type
//    generate_report_success || When report generates successfully || master_key, device_type, test_type
//    generate_report_failed || When report generation fails || master_key, device_type,  test_type, reason
}