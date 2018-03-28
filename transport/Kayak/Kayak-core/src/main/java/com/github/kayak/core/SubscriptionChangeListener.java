/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.core;

import java.util.EventListener;

public interface SubscriptionChangeListener  extends EventListener {

    public void addSubscription(Subscription s);

    public void subscribed(int id, boolean extended, Subscription s);

    public void unsubscribed(int id, boolean extended, Subscription s);

    public void subscriptionAllChanged(boolean all, Subscription s);

    public void subscriptionTerminated(Subscription s);
}
