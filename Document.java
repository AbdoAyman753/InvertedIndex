package sample;

public class Document {
    private String DocID;
    private String DocContent;

    public Document(String docID, String docContent) {
        DocID = docID;
        DocContent=docContent;
    }

    public String getDocID() {
        return DocID;
    }

    public void setDocID(String docID) {
        DocID = docID;
    }

    public String getDocContent() {
        return DocContent;
    }

    public void setDocContent(String docContent) {
        DocContent = docContent;
    }
}
