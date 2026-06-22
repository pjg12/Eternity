package eternity;

/**
 * Legacy compatibility wrapper for callers that still reference the old EXISTING training frame class.
 * The shared FrameTraining implementation now owns both layouts.
 */
class FrameTrainingExisting extends FrameTraining {
    private static final long serialVersionUID = 1L;

    FrameTrainingExisting(FrameSheet sheetFrame, StoreRuleManager ruleManager, StoreCharData character) {
        super(sheetFrame, ruleManager, character);
        showCard(CARD_EXISTING);
    }

    FrameTrainingExisting(FrameSheet sheetFrame, StoreRuleManager ruleManager) {
        this(sheetFrame, ruleManager, null);
    }
}
