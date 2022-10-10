package tv.formuler.mytvonline.technic

data class CourseData(
    var class_lcode: Int=0, // 과목코드 lCode
    var class_lname: String="", // 소제목 lName
    var class_lcontent: String="", // 과목소개 lContent
    var class_lsubname: String="", // 제목 lSubNmae
    var class_lurl: String="", // URL lURL
    var class_ldate: String="", // 과목 날짜 lDate
    var class_progress: Int=0, //진행도
    var class_lhour: String="" // 날짜

//    var class_sSchool: String=""
//    var class_sCode: String=""
//    var class_lsubcode: Int=0
//    var class_1hour: String=""
//    var class_1startTime: String=""
//    var class_1endTime: String=""
//    var class_classCode: Int=0
//    var class_teacherCode: Int=0

)