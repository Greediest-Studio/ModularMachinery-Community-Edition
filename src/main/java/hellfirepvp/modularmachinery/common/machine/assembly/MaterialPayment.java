package hellfirepvp.modularmachinery.common.machine.assembly;

/** 材料已经取出；放置失败时只执行一次退款。 */
public final class MaterialPayment {
    public static final MaterialPayment FREE = new MaterialPayment(() -> {});
    private Runnable refund;

    public MaterialPayment(Runnable refund) { this.refund = refund; }

    public void refund() {
        Runnable action = refund;
        refund = () -> {};
        action.run();
    }
}
