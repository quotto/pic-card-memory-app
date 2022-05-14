package net.wackwack.pic_card_memory.suite

import net.wackwack.pic_card_memory.SettingsActivityTest
import net.wackwack.pic_card_memory.SettingsRepositoryImplTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(SettingsActivityTest::class,SettingsRepositoryImplTest::class)
class UnitTestSuite {
}