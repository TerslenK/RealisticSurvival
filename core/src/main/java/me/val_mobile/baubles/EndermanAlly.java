/*
    Copyright (C) 2022  Val_Mobile

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.val_mobile.baubles;

import me.val_mobile.utils.RSVPet;
import me.val_mobile.utils.Utils;
import org.bukkit.persistence.PersistentDataType;

public interface EndermanAlly extends RSVPet {

    @Override
    default void addNbtData() {
        Utils.addNbtTag(getEntity(), "rsvmob", "enderman_ally", PersistentDataType.STRING);
        Utils.addNbtTag(getEntity(), "rsvendermanallyowner", getOwner().getUniqueId().toString(), PersistentDataType.STRING);
    }
}
