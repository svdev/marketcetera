package org.marketcetera.trade;

import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.util.log.I18NBoundMessage1P;
import org.marketcetera.util.log.I18NBoundMessage2P;
import org.marketcetera.quickfix.FIXDataDictionary;
import org.marketcetera.quickfix.FIXMessageUtil;
import org.marketcetera.core.IDFactory;
import org.marketcetera.core.NoMoreIDsException;
import org.marketcetera.core.InMemoryIDFactory;
import quickfix.*;
import quickfix.field.*;

import java.util.*;

/* $License$ */
/**
 * Factory for creating various messages.
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since $Release$
 */
@ClassVersion("$Id$") //$NON-NLS-1$
class FactoryImpl extends Factory {
    @Override
    public OrderSingle createOrderSingle() {
        OrderSingleImpl order = new OrderSingleImpl();
        assignOrderID(order);
        return order;
    }

    @Override
    public OrderSingleSuggestion createOrderSingleSuggestion() {
        return new OrderSingleSuggestionImpl();
    }

    @Override
    public OrderCancel createOrderCancel(ExecutionReport inLatestReport) {
        OrderCancelImpl order = new OrderCancelImpl();
        assignOrderID(order);
        if (inLatestReport != null) {
            order.setAccount(inLatestReport.getAccount());
            order.setDestinationID(inLatestReport.getDestinationID());
            order.setOriginalOrderID(inLatestReport.getOrderID());
            order.setQuantity(inLatestReport.getLeavesQuantity());
            order.setSide(inLatestReport.getSide());
            order.setSymbol(inLatestReport.getSymbol());
        }
        return order;
    }

    @Override
    public OrderReplace createOrderReplace(ExecutionReport inLatestReport) {
        OrderReplaceImpl order = new OrderReplaceImpl();
        assignOrderID(order);
        if (inLatestReport != null) {
            order.setAccount(inLatestReport.getAccount());
            order.setDestinationID(inLatestReport.getDestinationID());
            order.setOrderType(inLatestReport.getOrderType());
            order.setOriginalOrderID(inLatestReport.getOrderID());
            order.setPrice(inLatestReport.getLastPrice());
            order.setQuantity(inLatestReport.getLeavesQuantity());
            order.setSide(inLatestReport.getSide());
            order.setSymbol(inLatestReport.getSymbol());
            order.setTimeInForce(inLatestReport.getTimeInForce());
            order.setOrderCapacity(inLatestReport.getOrderCapacity());
            order.setPositionEffect(inLatestReport.getPositionEffect());
        }
        return order;
    }

    @Override
    public FIXOrder createOrder(Message inMessage, DestinationID inDestinationID)
            throws MessageCreationException {
        return new FIXOrderImpl(inMessage, inDestinationID);
    }

    @Override
    public ExecutionReport createExecutionReport(
            Message inMessage,
            DestinationID inDestinationID, Originator inOriginator)
            throws MessageCreationException {
        if(inMessage == null) {
            throw new NullPointerException();
        }
        if(inOriginator == null) {
            throw new NullPointerException();
        }
        if(FIXMessageUtil.isExecutionReport(inMessage)) {
            return new ExecutionReportImpl(inMessage, inDestinationID,
                    inOriginator);
        } else {
            throw new MessageCreationException(new I18NBoundMessage1P(
                    Messages.NOT_EXECUTION_REPORT, inMessage.toString()));
        }
    }

    @Override
    public ExecutionReport createExecutionReport(
            Message inMessage, DestinationID inDestinationID)
            throws MessageCreationException {
        return createExecutionReport(inMessage, inDestinationID,
                Originator.Server);
    }

    @Override
    public OrderCancelReject createOrderCancelReject(
            Message inMessage,
            DestinationID inDestinationID)
            throws MessageCreationException {
        if(inMessage == null) {
            throw new NullPointerException();
        }
        if(FIXMessageUtil.isCancelReject(inMessage)) {
            return new OrderCancelRejectImpl(inMessage, inDestinationID);
        } else {
            throw new MessageCreationException(new I18NBoundMessage1P(
                    Messages.NOT_CANCEL_REJECT, inMessage.toString()));
        }
    }

    @Override
    public OrderSingle createOrderSingle(Message inMessage, 
                                         DestinationID inDestinationID)
            throws MessageCreationException {
        checkSystemMessage(inMessage);
        if(!FIXMessageUtil.isOrderSingle(inMessage)) {
            throw new MessageCreationException(new I18NBoundMessage1P(
                    Messages.NOT_SINGLE_ORDER, inMessage.toString()));
        }
        OrderSingle order = createOrderSingle();
        order.setAccount(FIXUtil.getAccount(inMessage));
        order.setDestinationID(inDestinationID);
        order.setCustomFields(getFieldMap(inMessage, ORDER_SINGLE_FIELDS));
        order.setOrderID(FIXUtil.getOrderID(inMessage));
        order.setOrderType(FIXUtil.getOrderType(inMessage));
        order.setPrice(FIXUtil.getPrice(inMessage));
        order.setQuantity(FIXUtil.getOrderQuantity(inMessage));
        order.setSide(FIXUtil.getSide(inMessage));
        order.setSymbol(FIXUtil.getSymbol(inMessage));
        order.setTimeInForce(FIXUtil.getTimeInForce(inMessage));
        order.setOrderCapacity(FIXUtil.getOrderCapacity(inMessage));
        order.setPositionEffect(FIXUtil.getPositionEffect(inMessage));
        assignOrderID(order);
        return order;
    }

    @Override
    public OrderCancel createOrderCancel(Message inMessage,
                                         DestinationID inDestinationID)
            throws MessageCreationException {
        checkSystemMessage(inMessage);
        if(!FIXMessageUtil.isCancelRequest(inMessage)) {
            throw new MessageCreationException(new I18NBoundMessage1P(
                    Messages.NOT_CANCEL_ORDER, inMessage.toString()));
        }
        OrderCancel order = new OrderCancelImpl();
        order.setAccount(FIXUtil.getAccount(inMessage));
        order.setDestinationID(inDestinationID);
        order.setCustomFields(getFieldMap(inMessage, ORDER_CANCEL_FIELDS));
        order.setOrderID(FIXUtil.getOrderID(inMessage));
        order.setOriginalOrderID(FIXUtil.getOriginalOrderID(inMessage));
        order.setQuantity(FIXUtil.getOrderQuantity(inMessage));
        order.setSide(FIXUtil.getSide(inMessage));
        order.setSymbol(FIXUtil.getSymbol(inMessage));
        assignOrderID(order);
        return order;
    }

    @Override
    public OrderReplace createOrderReplace(
            Message inMessage,
            DestinationID inDestinationID)
            throws MessageCreationException {
        checkSystemMessage(inMessage);
        if(!FIXMessageUtil.isCancelReplaceRequest(inMessage)) {
            throw new MessageCreationException(new I18NBoundMessage1P(
                    Messages.NOT_CANCEL_REPLACE_ORDER, inMessage.toString()));
        }
        OrderReplace order = new OrderReplaceImpl();
        order.setAccount(FIXUtil.getAccount(inMessage));
        order.setDestinationID(inDestinationID);
        order.setCustomFields(getFieldMap(inMessage, ORDER_REPLACE_FIELDS));
        order.setOrderID(FIXUtil.getOrderID(inMessage));
        order.setOrderType(FIXUtil.getOrderType(inMessage));
        order.setOriginalOrderID(FIXUtil.getOriginalOrderID(inMessage));
        order.setPrice(FIXUtil.getPrice(inMessage));
        order.setQuantity(FIXUtil.getOrderQuantity(inMessage));
        order.setSide(FIXUtil.getSide(inMessage));
        order.setSymbol(FIXUtil.getSymbol(inMessage));
        order.setTimeInForce(FIXUtil.getTimeInForce(inMessage));
        order.setOrderCapacity(FIXUtil.getOrderCapacity(inMessage));
        order.setPositionEffect(FIXUtil.getPositionEffect(inMessage));
        assignOrderID(order);
        return order;
    }

    private void assignOrderID(OrderBase inOrder) {
        try {
            inOrder.setOrderID(new OrderID(mIDFactory.getNext()));
        } catch (NoMoreIDsException e) {
            //Indicates that id factories are not correctly assembled
            //to prevent failure. The factory should not throw exceptions.
            //In case it's unable to generate IDs, it should rely on a
            //local, in-memory ID generator that cannot fail
            Messages.UNABLE_TO_GENERATE_IDS.error(this, e);
            throw new IllegalArgumentException(
                    Messages.UNABLE_TO_GENERATE_IDS.getText(), e);
        }
    }
    private IDFactory mIDFactory = new InMemoryIDFactory(
            System.currentTimeMillis());

    /**
     * Returns all the fields contained in the supplied message as a map.
     * The map has the field tag number as the key and the field string
     * value as the value.
     * <p>
     * The returned map excludes the set of fields supplied in
     * <code>inExcludeFields</code>.
     * <p>
     * Byte Fields are currently not supported. If the supplied message
     * includes any byte fields, an exception will be thrown.
     *
     * @param inMessage The message whose fields need to be converted into
     * the map.
     * @param inExcludeFields The message fields that should not be included
     * in the returned map.
     *
     * @return the map containing fields of the supplied message.
     *
     * @throws MessageCreationException if there were errors.
     */
    static Map<String,String> getFieldMap(Message inMessage,
                                          Set<Integer> inExcludeFields)
            throws MessageCreationException {
        Map<String,String> fields = new HashMap<String, String>();
        Iterator<Field<?>> iterator = inMessage.iterator();
        while(iterator.hasNext()) {
            Field<?> f = iterator.next();
            if(inExcludeFields != null && inExcludeFields.contains(f.getTag())) {
                continue;
            }
            //all fix fields except the one's that contain binary data
            //are stored as string fields. All of them can be safely
            //retrieved as string fields.
            if(f instanceof StringField) {
                fields.put(String.valueOf(f.getTag()),
                        ((StringField)f).getObject());
            }
            //ignore fields that contain binary data.
        }
        return fields.isEmpty()
                ? null
                : fields;
    }

    /**
     * Verify if the supplied message is a system FIX message, ie. it's
     * not a regular FIX message. And that it does not contain any groups.
     *
     * @param inMessage the message to be verified. Cannot be null.
     *
     * @throws MessageCreationException if the message fails any of the checks.
     */
    private void checkSystemMessage(Message inMessage)
            throws MessageCreationException {
        if(inMessage == null) {
            throw new NullPointerException();
        }
        try {
            //Verify that the message is a System FIX Message, not a
            //regular FIX Message.
            String beginString = inMessage.getHeader().getField(
                    new BeginString()).getValue();
            if(!FIXDataDictionary.FIX_SYSTEM_BEGIN_STRING.equals(beginString)) {
                throw new MessageCreationException(new I18NBoundMessage1P(
                        Messages.NON_SYSTEM_FIX_MESSAGE,beginString));
            }
        } catch (FieldNotFound inFieldNotFound) {
            throw new MessageCreationException(inFieldNotFound,
                    new I18NBoundMessage1P(
                            Messages.SYSTEM_FIX_MESSAGE_NO_BEGIN_STRING,
                            inMessage.toString()));
        }
        //Verify that the message does not contain any groups as
        //we do not support messages with groups.
        Iterator<Integer> iterator = inMessage.groupKeyIterator();
        if(iterator.hasNext()) {
            throw new MessageCreationException(new I18NBoundMessage2P(
                    Messages.MESSAGE_HAS_GROUPS, iterator.next(),
                    inMessage.toString()));
        }
    }
    static final Set<Integer> ORDER_SINGLE_FIELDS;
    static final Set<Integer> ORDER_CANCEL_FIELDS;
    static final Set<Integer> ORDER_REPLACE_FIELDS;
    static {
        Set<Integer> tmp = new HashSet<Integer>();
        tmp.addAll(Arrays.asList(
                ClOrdID.FIELD,
                Account.FIELD,
                OrdType.FIELD,
                Price.FIELD,
                OrderQty.FIELD,
                quickfix.field.Side.FIELD,
                Symbol.FIELD,
                quickfix.field.SecurityType.FIELD,
                quickfix.field.TimeInForce.FIELD,
                quickfix.field.OrderCapacity.FIELD,
                quickfix.field.PositionEffect.FIELD
        ));
        ORDER_SINGLE_FIELDS = Collections.unmodifiableSet(tmp);
        tmp = new HashSet<Integer>();
        tmp.addAll(Arrays.asList(
                ClOrdID.FIELD,
                Account.FIELD,
                OrigClOrdID.FIELD,
                OrderQty.FIELD,
                quickfix.field.Side.FIELD,
                Symbol.FIELD,
                quickfix.field.SecurityType.FIELD
        ));
        ORDER_CANCEL_FIELDS = Collections.unmodifiableSet(tmp);
        tmp = new HashSet<Integer>();
        tmp.addAll(Arrays.asList(
                ClOrdID.FIELD,
                Account.FIELD,
                OrdType.FIELD,
                OrigClOrdID.FIELD,
                Price.FIELD,
                OrderQty.FIELD,
                quickfix.field.Side.FIELD,
                Symbol.FIELD,
                quickfix.field.SecurityType.FIELD,
                quickfix.field.TimeInForce.FIELD,
                quickfix.field.OrderCapacity.FIELD,
                quickfix.field.PositionEffect.FIELD
        ));
        ORDER_REPLACE_FIELDS = Collections.unmodifiableSet(tmp);
    }
}