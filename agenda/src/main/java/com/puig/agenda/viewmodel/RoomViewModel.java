package com.puig.agenda.viewmodel;

import java.util.List;

public class RoomViewModel {
    private String roomName;
    private List<WeekViewModel> weekMonth;

    public RoomViewModel() {};

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<WeekViewModel> getWeekMonth() {
        return weekMonth;
    }

    public void setWeekMonth(List<WeekViewModel> weekMonth) {
        this.weekMonth = weekMonth;
    }
}
