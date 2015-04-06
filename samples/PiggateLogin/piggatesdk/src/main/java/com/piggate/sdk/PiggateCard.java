package com.piggate.sdk;

//Piggate class for credit cards
public class PiggateCard {

    private String _cardNumber;
    private String _CVC;
    private int _month;
    private int _year;
    private String _tokenID;

    //Public basic constructor
    public PiggateCard(String cardNumber, String CVC, int month, int year){
        setCardNumber(cardNumber);
        setCVC(CVC);
        setMonth(month);
        setYear(year);
    }

    //Public constructor with token
    public PiggateCard(String cardNumber, String CVC, int month, int year, String tokenID){
        setCardNumber(cardNumber);
        setCVC(CVC);
        setMonth(month);
        setYear(year);
        setTokenID(tokenID);
    }

    public String getCardNumber() {
        return _cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this._cardNumber = cardNumber;
    }

    public String getCVC() {
        return _CVC;
    }

    public void setCVC(String CVC) {
        this._CVC = CVC;
    }

    public int getMonth() {
        return _month;
    }

    public void setMonth(int month) {
        this._month = month;
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        this._year = year;
    }

    public String getTokenID() {
        return _tokenID;
    }

    public void setTokenID(String tokenID) {
        this._tokenID = tokenID;
    }
}
