
@Grab("org.jsoup:jsoup:1.12.1")
import org.jsoup.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

println "started"

final def timeStamp = System.currentTimeMillis()

File file = new File(timeStamp + "_topic.csv")
def url = "forum28.html"

processTopicPage(url, file)


private void getSubForumPage(File file, String topicUrl) {
    println "page:" + topicUrl
    Document document = getDoc('http://extreme.by/forum/' + topicUrl,0)
    processSubforum(document, file)
}

private List<Element> processSubforum(Document document, File file) {
    //TODO -------------------------
    document.select(".postbody").each { writePostInToFile(it, file) }

    //Следующая страница
    Element nextPage = document.select("a:contains(Следующая страница)").first()
    if (nextPage != null) {
        getSubForumPage(file, nextPage.select("A").first().attr("href"))
    }
}


private void processTopicPage(String url, File file) {
    println "page:" + url
    Document document = getDoc("http://extreme.by/forum/" + url, 0)
    processTopic(document, file)
}

private List<Element> processTopic(Document document, File file) {
    document.select(".topictitle").each { writeTopicToFile(it, file) }

    //Следующая страница
    Element nextPage = document.select("a:contains(Следующая страница)").first()
    if (nextPage != null) {
        processTopicPage(nextPage.select("A").first().attr("href"), file)
    }
}

private void writeTopicToFile(Element it, File file) {
    if (it.tagName().equals("span")) {
        def link = it.select("a")
        def linkVal = link.attr("href")
        def name = link.text()
        println "write line: " + name
        def fileName = name.replaceAll("[/\\.,: ]+", "_") + ".txt"
        println "filename: ${fileName}"
        file << ("${linkVal};${name}\n")
        //тут тащим субфорум
        getSubForumPage(new File(fileName), linkVal)
    }
}

private void writePostInToFile(Element it, File file) {
    try {
        def message = it.text()
        def user = it.parent().parent().parent().parent().parent().parent().select(".name").text()
        def date = it.parent().parent().parent().select(".postdetails").first().html()

        String string = "${user}\n" +
                "_______________________________\n" +
                "${date}\n" +
                "_______________________________\n" +
                "${message}\n\n\n\n"
        file << string
        println message
    } catch (Exception e) {

    }

}

private Document getDoc(String url, int retry) {
    def document = null
    if (retry < 100) {
        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                    .header("Accept-Language", "ru-RU")
                    .header("Content-Type", "text/html")
                    .header("charset", "windows-1251")
                    .header("Accept-Encoding", "identity")
                    .timeout(0).get()
        }catch (Exception e){
            retry++
            println "retry: ${retry} cause ${e}"
           document= getDoc(url, retry)
        }
    }

    document
}


