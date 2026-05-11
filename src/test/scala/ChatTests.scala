import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ChatLogic.*


class ChatLogicSpec extends AnyFlatSpec with Matchers {
  
  "validateNickname" should "return None for empty string" in {
    validateNickname("") shouldBe None
    validateNickname("   ") shouldBe None
  }
  
  it should "return trimmed nickname for valid input" in {
    validateNickname("  Alice  ") shouldBe Some("Alice")
    validateNickname("Bob") shouldBe Some("Bob")
  }
  
  "formatUserMessage" should "add brackets around nickname" in {
    formatUserMessage("Alice", "Hello!") shouldBe "[Alice] Hello!"
  }
}