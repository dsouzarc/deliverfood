package com.ryan.deliverfood;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Order {

    private final String myName;
    private final String myNumber;
    private final String myAddress;
    private final String restaurantName;
    private final String uniqueDeviceIdentifier;
    private final String[] myOrder;
    private final String idNumber;
    private final String calendarTimeMillis;
    private final Calendar theDate;

    private STATUS status;
    private String orderCost;
    private String estimatedDeliveryTime = "";
    private boolean isClaimed = false;

    //Name, phone number, my address, restaurant address, UID, myOrder[], order ID, orderCost, time in millis, status

    public Order(String myName, String myNumber, String myAddress, String restaurantName,
                 String uniqueDeviceIdentifier, String[] myOrder, String idNumber, String orderCost,
                 final String calendarTimeMillis, final String status) {
        this.myName = myName;
        this.myNumber = myNumber;
        this.myAddress = myAddress;
        this.restaurantName = restaurantName;
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
        this.myOrder = myOrder;
        this.idNumber = idNumber;
        this.orderCost = orderCost;
        this.calendarTimeMillis = calendarTimeMillis;
        this.theDate = new GregorianCalendar();
        this.theDate.setTimeInMillis(Long.parseLong(calendarTimeMillis));
        this.status = getStatus(status);

        this.isClaimed = this.status == STATUS.UNCLAIMED;
    }

    public static enum STATUS {
        UNCLAIMED, CLAIMED, FOOD_ORDERED, EN_ROUTE, DELIVERED;
    }

    public STATUS getStatus(final String text) {
        if(text.contains("0")) {
            return STATUS.UNCLAIMED;
        }
        this.isClaimed = true;
        if(text.contains("1")) {
            return STATUS.CLAIMED;
        }
        else if(text.contains("2")) {
            return STATUS.FOOD_ORDERED;
        }
        else if(text.contains("3")) {
            return STATUS.EN_ROUTE;
        }
        else if(text.contains("4")) {
            return STATUS.DELIVERED;
        }
        this.isClaimed = false;
        return STATUS.UNCLAIMED;
    }

    public STATUS getStatus(final int val) {
        switch (val) {
            case 0:
                return STATUS.UNCLAIMED;
            case 1:
                this.isClaimed = true;
                return STATUS.CLAIMED;
            case 2:
                this.isClaimed = true;
                return STATUS.FOOD_ORDERED;
            case 3:
                this.isClaimed = true;
                return STATUS.EN_ROUTE;
            case 4:
                this.isClaimed = true;
                return STATUS.DELIVERED;
            default:
                return STATUS.UNCLAIMED;
        }
    }

    public String getStatusNiceString() {
        switch (status) {
            case UNCLAIMED:
                return "Unclaimed";
            case CLAIMED:
                return "Claimed";
            case FOOD_ORDERED:
                return "Food Ordered";
            case EN_ROUTE:
                return "En Route";
            case DELIVERED:
                return "Delivered";
            default:
                return "Unclaimed";
        }
    }

    public STATUS getStatus() {
        return this.status;
    }

    public static String getStatusIC(final STATUS status) {
        switch (status) {
            case UNCLAIMED:
                return "\uD83D\uDD0E";
            case CLAIMED:
                return "\uD83D\uDC4D";
            case FOOD_ORDERED:
                return "\uD83C\uDF73";
            case EN_ROUTE:
                return "\uD83D\uDE98";
            case DELIVERED:
                return "\u2714";
            default:
                return "\uD83D\uDD0E";
        }
    }

    public String getStatusIC() {
        return getStatusIC(status);
    }

    public JSONObject toJSONObject() {
        final JSONObject theObject = new JSONObject();

        try {
            theObject.put("myName", myName);
            theObject.put("myNumber", myNumber);
            theObject.put("myAddress", myAddress);
            theObject.put("restaurantName", restaurantName);
            theObject.put("uniqueDeviceIdentifier", uniqueDeviceIdentifier);
            theObject.put("myOrder", new JSONArray(myOrder));
            theObject.put("id", idNumber);
            theObject.put("orderCost", orderCost);
            theObject.put("time", calendarTimeMillis);
            theObject.put("status", status);
            theObject.put("deliveryTime", estimatedDeliveryTime);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return theObject;
    }

    public String getUpdateOrderHttpPost() {
        return String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?id=%s&udid=%s&checkOrder=%s&user=%s",
                Uri.encode(idNumber), Uri.encode(uniqueDeviceIdentifier), Uri.encode("1"), Uri.encode("1"));
    }

    public String getOrderDeleteHttpPost() {
        return String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?id=%s&udid=%s&deleteOrder=%s",
                Uri.encode(idNumber), Uri.encode(uniqueDeviceIdentifier), Uri.encode("1"));
    }

    public String getOrderHttpPost() {
        final StringBuilder newOrder = new StringBuilder("");
        for(String order : myOrder) {
            newOrder.append(order + "||");
        }
        final String orderString = newOrder.substring(0, newOrder.length() - 2);

        return String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?id=%s&udid=%s&PhoneNumber=%s&" +
                        "Name=%s&Restaurant=%s&OrderDetails=%s&Address=%s&user=%s&newOrder=%s&EstimatedCost=%s",
                Uri.encode(idNumber), Uri.encode(uniqueDeviceIdentifier), Uri.encode(myNumber), Uri.encode(myName),
                Uri.encode(restaurantName), Uri.encode(orderString), Uri.encode(myAddress),
                Uri.encode("1"), Uri.encode("1"), Uri.encode(orderCost));
    }

    public String getOrderDriverDeliveryHttpPost() {
        return String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?id=%s&udid=%s&getOrder=%s",
                Uri.encode(idNumber), Uri.encode(uniqueDeviceIdentifier), Uri.encode("1"));
    }

    public String getCheckOrderHttpPost() {
        return String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?id=%s&udid=%s&checkOrder=%s",
                Uri.encode(idNumber), Uri.encode(uniqueDeviceIdentifier), Uri.encode("1"));
    }

    public static Order getOrder(final String orderAsJSON) {
        try {
            return getOrder(new JSONObject(orderAsJSON));
        }
        catch (Exception e) {
            return null;
        }
    }

    public static Order getOrder(final JSONObject theJSON) {

        try {
            final JSONArray theOrder = theJSON.getJSONArray("myOrder");

            final String[] theItems = new String[theOrder.length()];
            for(int i = 0; i < theOrder.length(); i++) {
                theItems[i] = theOrder.get(i).toString();
            }

            final String estimatedDeliver = theJSON.getString("deliveryTime");

            final Order toReturn = new Order(theJSON.getString("myName"), theJSON.getString("myNumber"),
                    theJSON.getString("myAddress"), theJSON.getString("restaurantName"),
                    theJSON.getString("uniqueDeviceIdentifier"), theItems,
                    theJSON.getString("id"), theJSON.getString("orderCost"),
                    theJSON.getString("time"), theJSON.getString("status"));
            toReturn.setEstimatedDeliveryTime(estimatedDeliver);
            return toReturn;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void incrementStatus() {
        switch (this.status) {
            case UNCLAIMED:
                this.status = STATUS.CLAIMED;
                this.isClaimed = true;
                break;
            case CLAIMED:
                this.status = STATUS.FOOD_ORDERED;
                break;
            case FOOD_ORDERED:
                this.status = STATUS.EN_ROUTE;
                break;
            case EN_ROUTE:
                this.status = STATUS.DELIVERED;
                break;
            default:
                break;
        }
    }

    public String getCalendarTimeMillis() {
        return this.calendarTimeMillis;
    }

    public String getDateForm() {
        return theDate.get(Calendar.MONTH) + "/" + theDate.get(Calendar.DAY_OF_MONTH) +
                "/" + theDate.get(Calendar.YEAR) + " at " +
                theDate.get(Calendar.HOUR_OF_DAY) + ":" + theDate.get(Calendar.MINUTE);
    }

    public static String getDateForm(final long timeInMillis) {
        final GregorianCalendar theCal = new GregorianCalendar();
        theCal.setTimeInMillis(timeInMillis);
        return theCal.get(Calendar.MONTH) + "/" + theCal.get(Calendar.DAY_OF_MONTH) +
                "/" + theCal.get(Calendar.YEAR) + " at " +
                theCal.get(Calendar.HOUR_OF_DAY) + ":" + theCal.get(Calendar.MINUTE);
    }

    public void claim() {
        if(this.status == STATUS.UNCLAIMED) {
            this.status = STATUS.CLAIMED;
            this.isClaimed = true;
        }
    }

    public boolean isClaimed() {
        return this.isClaimed;
    }

    public String getDeliveryTime() {
        return this.estimatedDeliveryTime.replace("\n", "");
    }
    public void setEstimatedDeliveryTime(final String time) {
        this.estimatedDeliveryTime = time;
    }

    public Calendar getCalendar() {
        return this.theDate;
    }

    public String getMyName() {
        return myName;
    }

    public String getMyNumber() {
        return myNumber;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }

    public String[] getMyOrder() {
        return myOrder;
    }

    public String getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(String orderCost) {
        this.orderCost = orderCost;
    }

    public void setOrderStatus(final String newStatus) {
        this.status = getStatus(newStatus);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getOrderForm() {
        return "IsClaimed: " + isClaimed + ". Restaurant: " + restaurantName +
                " Order cost: " + orderCost + " Status: " + status;
    }

    @Override
    public boolean equals(Object other) {
        if(this == other)
            return true;
        if(!(other instanceof Order))
            return false;
        return ((Order)other).getIdNumber().equals(this.getIdNumber());
    }

    /*@Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof com.ryan.bringmefood.Order))
            return false;

        Order order = (Order) o;

        if (!calendarTimeMillis.equals(order.calendarTimeMillis))
            return false;
        if (!idNumber.equals(order.idNumber))
            return false;
        if (!myAddress.equals(order.myAddress))
            return false;
        if (!myName.equals(order.myName))
            return false;
        if (!myNumber.equals(order.myNumber))
            return false;
        if (!Arrays.equals(myOrder, order.myOrder))
            return false;
        if (!orderCost.equals(order.orderCost))
            return false;
        if (!restaurantName.equals(order.restaurantName))
            return false;
        if (!status.equals(order.status))
            return false;
        if (!theDate.equals(order.theDate))
            return false;
        if (!uniqueDeviceIdentifier.equals(order.uniqueDeviceIdentifier))
            return false;
        return true;
    }*/

    @Override
    public String toString() {
        final StringBuilder items = new StringBuilder();
        for(String item : myOrder) {
            items.append(", " + item);
        }

        final String calendar = (theDate.get(Calendar.MONTH) + 1) + "/" +
                theDate.get(Calendar.DAY_OF_MONTH) + "/" + theDate.get(Calendar.YEAR);

        return "Order{" +
                "myAddress='" + myAddress + '\'' +
                ", myName='" + myName + '\'' +
                ", myNumber='" + myNumber + '\'' +
                ", restaurantName='" + restaurantName + '\'' +
                ", uniqueDeviceIdentifier='" + uniqueDeviceIdentifier + '\'' +
                ", theDate=" + calendar +
                ", status='" + status + '\'' +
                ", orderCost='" + orderCost + '\'' +
                ", estimatedDeliveryTime='" + estimatedDeliveryTime + '\'' +
                ", Items=' " + items.toString() + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = myName.hashCode();
        result = 31 * result + myNumber.hashCode();
        result = 31 * result + myAddress.hashCode();
        result = 31 * result + restaurantName.hashCode();
        result = 31 * result + uniqueDeviceIdentifier.hashCode();
        result = 31 * result + Arrays.hashCode(myOrder);
        result = 31 * result + orderCost.hashCode();
        result = 31 * result + idNumber.hashCode();
        result = 31 * result + calendarTimeMillis.hashCode();
        result = 31 * result + theDate.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }

    public void log(final String message) {
        Log.e("com.ryan.deliverfood", message);
    }
}