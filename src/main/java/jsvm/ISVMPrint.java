package jsvm;

public interface ISVMPrint
{
    ISVMPrint NO_PRINT = s -> { };

    void print(String s);
}
