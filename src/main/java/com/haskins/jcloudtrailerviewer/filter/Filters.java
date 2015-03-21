/*    
CloudTrail Log Viewer, is a Java desktop application for reading AWS CloudTrail
logs files.

Copyright (C) 2015  Mark P. Haskins

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.haskins.jcloudtrailerviewer.filter;

import com.haskins.jcloudtrailerviewer.model.Event;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A container object to which multiple Filters can be added so that the can all
 * be checked at the same time.
 * 
 * @author mark
 */
public class Filters {
    
    private final List<EventFilter> filters = new ArrayList<>();
    
    public void addEventFilter(EventFilter filter) {
        this.filters.add(filter);
    }
    
    public void setFilterCriteria(String value) {
        
        for (EventFilter filter : filters) {
            filter.setNeedle(value);
        }
    }
    
    public CopyOnWriteArrayList<Event> filterEvents(Collection<Event> events) {
        
        CopyOnWriteArrayList<Event> filteredEvents = new CopyOnWriteArrayList<>();
        
        for (Event event : events) {
            
            boolean passed = true;
            
            for (EventFilter filter : filters) {
                
                passed &= filter.passesFilter(event);
            }
            
            if (passed) {
                
                filteredEvents.add(event);
            }
        } 
        
        return filteredEvents;
    } 
    
    public boolean passesFilter(Event event) {
        
        boolean passed = false;
        
        for (EventFilter filter : filters) {

            passed |= filter.passesFilter(event);
        }
        
        return passed;
    }
}
