package net.consensys.pantheon.ethereum.core;

import java.util.ArrayDeque;
import java.util.Deque;

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Gas;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.core.WorldUpdater;
import tech.pegasys.pantheon.ethereum.vm.BlockHashLookup;
import tech.pegasys.pantheon.ethereum.vm.Code;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame.Type;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class MessageFrameTestFixture {

  private static final Address DEFAUT_ADDRESS = AddressHelpers.ofValue(244259721);

  private Type type = Type.MESSAGE_CALL;
  private Deque<MessageFrame> messageFrameStack = new ArrayDeque<>();
  private Blockchain blockchain;
  private WorldUpdater worldState;
  private Gas initialGas = Gas.MAX_VALUE;
  private Address address = DEFAUT_ADDRESS;
  private Address sender = DEFAUT_ADDRESS;
  private Address originator = DEFAUT_ADDRESS;
  private Address contract = DEFAUT_ADDRESS;
  private Wei gasPrice = Wei.ZERO;
  private Wei value = Wei.ZERO;
  private BytesValue inputData = BytesValue.EMPTY;
  private Code code = new Code(BytesValue.EMPTY);
  private BlockHeader blockHeader = new BlockHeaderTestFixture().buildHeader();
  private int depth = 0;

  public MessageFrameTestFixture type(final Type type) {
    this.type = type;
    return this;
  }

  public MessageFrameTestFixture messageFrameStack(final Deque<MessageFrame> messageFrameStack) {
    this.messageFrameStack = messageFrameStack;
    return this;
  }

  public MessageFrameTestFixture blockchain(final Blockchain blockchain) {
    this.blockchain = blockchain;
    return this;
  }

  public MessageFrameTestFixture worldState(final WorldUpdater worldState) {
    this.worldState = worldState;
    return this;
  }

  public MessageFrameTestFixture initialGas(final Gas initialGas) {
    this.initialGas = initialGas;
    return this;
  }

  public MessageFrameTestFixture sender(final Address sender) {
    this.sender = sender;
    return this;
  }

  public MessageFrameTestFixture address(final Address address) {
    this.address = address;
    return this;
  }

  public MessageFrameTestFixture originator(final Address originator) {
    this.originator = originator;
    return this;
  }

  public MessageFrameTestFixture contract(final Address contract) {
    this.contract = contract;
    return this;
  }

  public MessageFrameTestFixture gasPrice(final Wei gasPrice) {
    this.gasPrice = gasPrice;
    return this;
  }

  public MessageFrameTestFixture value(final Wei value) {
    this.value = value;
    return this;
  }

  public MessageFrameTestFixture inputData(final BytesValue inputData) {
    this.inputData = inputData;
    return this;
  }

  public MessageFrameTestFixture code(final Code code) {
    this.code = code;
    return this;
  }

  public MessageFrameTestFixture blockHeader(final BlockHeader blockHeader) {
    this.blockHeader = blockHeader;
    return this;
  }

  public MessageFrameTestFixture depth(final int depth) {
    this.depth = depth;
    return this;
  }

  public MessageFrame build() {
    return MessageFrame.builder()
        .type(type)
        .messageFrameStack(messageFrameStack)
        .blockchain(blockchain)
        .worldState(worldState)
        .initialGas(initialGas)
        .address(address)
        .originator(originator)
        .gasPrice(gasPrice)
        .inputData(inputData)
        .sender(sender)
        .value(value)
        .apparentValue(value)
        .contract(contract)
        .code(code)
        .blockHeader(blockHeader)
        .depth(depth)
        .completer(c -> {})
        .miningBeneficiary(blockHeader.getCoinbase())
        .blockHashLookup(new BlockHashLookup(blockHeader, blockchain))
        .build();
  }
}
