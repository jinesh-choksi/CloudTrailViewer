/*
 * Copyright (C) 2015 Mark P. Haskins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.haskins.jcloudtrailerviewer.filter;

import com.haskins.jcloudtrailerviewer.AbstractTest;
import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark.haskins
 */
public class UsernameFilterTest extends AbstractTest {
    
    /**
     * Test of setNeedle method, of class UsernameFilter.
     */
    @Test
    public void testSetNeedle() {
        
        String expectedValue = "Lemming";
        UsernameFilter instance = new UsernameFilter();
        instance.setNeedle(expectedValue);
        
        String returnedValue = null;
        
        try {

            Field privateStringField = UsernameFilter.class.getDeclaredField("needle");
            privateStringField.setAccessible(true);
            returnedValue = (String)privateStringField.get(instance);
            
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException ex) {
            fail(ex.getMessage());
        }
        
        assertEquals(expectedValue, returnedValue);
    }

    /**
     * Test of passesFilter method, of class UsernameFilter.
     */
    @Test
    public void testPassesFilterWithSessionIssuer() {
        
        UsernameFilter instance = new UsernameFilter();
        instance.setNeedle("iam_role");
        
        boolean expResult = true;
        boolean result = instance.passesFilter(eventOne);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testPassesFilterWithoutSessionIssuer() {
        
        UsernameFilter instance = new UsernameFilter();
        instance.setNeedle("iam_role");
        
        boolean expResult = true;
        boolean result = instance.passesFilter(eventThree);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of passesFilter method, of class UsernameFilter.
     */
    @Test
    public void testFailsFilter() {
        
        UsernameFilter instance = new UsernameFilter();
        instance.setNeedle("Lemming");
        
        boolean expResult = false;
        boolean result = instance.passesFilter(eventOne);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of passesFilter method, of class FreeformFilter.
     */
    @Test
    public void testNoNeedle() {
        
        UsernameFilter instance = new UsernameFilter();
        
        boolean expResult = true;
        boolean result = instance.passesFilter(eventOne);
        
        assertEquals(expResult, result);
    }
    
}
