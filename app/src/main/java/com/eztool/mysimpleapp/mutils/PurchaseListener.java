package com.eztool.mysimpleapp.mutils;

public interface PurchaseListener {
    public void purchaseFailed(int item);
    public void purchaseSuccess(int item);
    public void purchaseCancel(int item);
}
