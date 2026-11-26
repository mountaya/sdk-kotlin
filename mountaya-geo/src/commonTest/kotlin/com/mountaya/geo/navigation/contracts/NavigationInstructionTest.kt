package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NavigationInstructionTest {
    @Test
    fun twoInstructionsAreEqualWhereTheKeyAndItsArgumentsAre() {
        val exit = NavigationInstruction(NavigationInstructionKey.RoundaboutExit, name = "Avenue Michel Croz", exit = 2)

        assertEquals(
            exit,
            NavigationInstruction(NavigationInstructionKey.RoundaboutExit, name = "Avenue Michel Croz", exit = 2),
        )
        assertEquals(
            exit.hashCode(),
            NavigationInstruction(NavigationInstructionKey.RoundaboutExit, "Avenue Michel Croz", 2).hashCode(),
        )
        assertNotEquals(
            exit,
            NavigationInstruction(NavigationInstructionKey.RoundaboutExit, name = "Avenue Michel Croz", exit = 3),
        )
        assertNotEquals(
            NavigationInstruction(NavigationInstructionKey.TurnLeftPlain),
            NavigationInstruction(NavigationInstructionKey.TurnRightPlain),
        )
        assertEquals("NavigationInstruction(roundaboutExit, name=Avenue Michel Croz, exit=2)", exit.toString())
    }
}
