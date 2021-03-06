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

/**
 * An EventFilter implementation that filters on Username
 * 
 * @author mark
 */
public class UsernameFilter implements EventFilter {
    
    private String needle;
    
    @Override
    public void setNeedle(String username) {
        
        this.needle = username;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    ///// Abstract implementations
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean passesFilter(Event event) {
        
        boolean passesFilter = false;
        
        if (needle == null || needle.trim().length() == 0) {
            
            passesFilter = true;
            
        } else {
        
            String value;
            
            if (event.getUserIdentity().getSessionContext() != null && event.getUserIdentity().getSessionContext().getSessionIssuer() != null) {
                value = event.getUserIdentity().getSessionContext().getSessionIssuer().getUserName();
            }
            else {
                value = event.getUserIdentity().getUserName();
            }
           
            if (value != null && value.equalsIgnoreCase(this.needle)) {

                passesFilter = true;
            }
        }
            
        return passesFilter;
    }
}
