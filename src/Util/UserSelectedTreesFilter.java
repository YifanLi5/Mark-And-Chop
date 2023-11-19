package Util;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.InteractableObject;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;

import java.util.HashMap;
import java.util.Map;

public class UserSelectedTreesFilter implements Filter<RS2Object> {
    HashMap<Position, String> userSelections;
    public UserSelectedTreesFilter(HashMap<Position, String> userSelections) {
        this.userSelections = userSelections;
    }

    @Override
    public boolean match(RS2Object rs2Object) {
        boolean isMatch = false;
        for(Map.Entry<Position, String> selection: userSelections.entrySet()) {
            if(selection.getValue().equals(rs2Object.getName())
                    && selection.getKey().equals(rs2Object.getPosition())) {
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }
}