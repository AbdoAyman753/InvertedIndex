package sample;

public class Word {

    private java.lang.String Name;
    private int Total;
    private int DocNum;
    private String Documents;

    public Word(String name) {
        DocNum=0;
        Name = name;
        Documents="";
    }

    public String getDocuments() {
        return Documents;
    }

    public void setDocuments(String documents) {
        Documents = documents;
    }





    public int getTotal() {
        return Total;
    }

    public void setTotal(int total) {
        Total = total;
    }

    public java.lang.String getName() {
        return Name;
    }

    public void setName(java.lang.String name) {
        Name = name;
    }

    public int getDocNum() {
        return DocNum;
    }

    public void setDocNum(int docNum) {
        DocNum = docNum;
    }
}
