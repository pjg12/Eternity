package eternity;

/**
 * Legacy compatibility wrapper for callers that still reference the old NEW training frame class.
 * The shared FrameTraining implementation now owns both layouts.
 */
class FrameTrainingNew extends FrameTraining {
    private static final long serialVersionUID = 1L;

    FrameTrainingNew(FrameSheet sheetFrame, StoreRuleManager ruleManager, StoreCharData character) {
        super(sheetFrame, ruleManager, character);
        showCard(CARD_NEW);
    }

    FrameTrainingNew(FrameSheet sheetFrame, StoreRuleManager ruleManager) {
        this(sheetFrame, ruleManager, null);
    }
}
