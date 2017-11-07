/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl171.net.api.bidi;

import bgu.spl171.net.srv.bidi.Connections;

/**
 *
 * @author bennyl
 */
public interface BidiMessagingProtocol<T> {

    void start(int connectionId, Connections<T> connections);
    
    void process(T message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}