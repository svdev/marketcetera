package org.marketcetera.util.except;

import org.marketcetera.core.ClassVersion;
import org.marketcetera.util.log.I18NMessage;
import org.marketcetera.util.log.LogUtils;

/**
 * An internationalized runtime exception.
 * 
 * @author tlerios
 * @version $Id$
 */

/* $License$ */

@ClassVersion("$Id$")
public class I18NRuntimeException
    extends RuntimeException
    implements I18NThrowable
{

    // CLASS DATA.

    private static final long serialVersionUID=1L;


    // INSTANCE DATA.

    private I18NMessage mMessage;
    private Object[] mParams;
    

    // CONSTRUCTORS.

    /**
     * Constructs a new throwable without a message or an underlying
     * cause.
     */

    public I18NRuntimeException() {}

    /**
     * Constructs a new throwable without a message, but with the
     * given underlying cause.
     *
     * @param cause The cause.
     */

    public I18NRuntimeException
        (Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a new throwable with the given message, but without
     * an underlying cause.
     *
     * @param message The message.
     * @param params The message parameters.
     */

    public I18NRuntimeException
        (I18NMessage message,
         Object... params)
    {
        super(LogUtils.getSimpleMessage(message,params));
        mMessage=message;
        mParams=params;
    }

    /**
     * Constructs a new throwable with the given message and
     * underlying cause.
     *
     * @param cause The cause.
     * @param message The message.
     * @param params The message parameters.
     */

    public I18NRuntimeException
        (Throwable cause,
         I18NMessage message,
         Object... params)
    {
        super(LogUtils.getSimpleMessage(message,params),cause);
        mMessage=message;
        mParams=params;
    }


    // I18NThrowable.

    @Override
    public String getLocalizedMessage()
    {
        return I18NExceptUtils.getLocalizedMessage(this);
    }

    @Override
    public String getDetail()
    {
        return I18NExceptUtils.getDetail(this);
    }

    @Override
    public String getLocalizedDetail()
    {
        return I18NExceptUtils.getLocalizedDetail(this);
    }
    
    @Override
    public I18NMessage getI18NMessage()
    {
        return mMessage;
    }
    
    @Override
    public Object[] getParams()
    {
        return mParams;
    }
}
