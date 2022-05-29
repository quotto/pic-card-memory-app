package net.wackwack.pic_card_memory.suite

import net.wackwack.pic_card_memory.BasicUseCaseTest
import net.wackwack.pic_card_memory.SettingsRepositoryImplTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(BasicUseCaseTest::class,SettingsRepositoryImplTest::class)
class UnitTestSuite