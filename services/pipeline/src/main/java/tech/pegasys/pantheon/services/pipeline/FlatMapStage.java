package tech.pegasys.pantheon.services.pipeline;

import java.util.function.Function;
import java.util.stream.Stream;

public class FlatMapStage<I, O> implements Runnable {

  private final InputPipe<I> inputPipe;
  private final OutputPipe<O> outputPipe;
  private final Function<I, Stream<O>> mapper;

  public FlatMapStage(
      final InputPipe<I> inputPipe,
      final OutputPipe<O> outputPipe,
      final Function<I, Stream<O>> mapper) {
    this.inputPipe = inputPipe;
    this.outputPipe = outputPipe;
    this.mapper = mapper;
  }

  @Override
  public void run() {
    while (inputPipe.hasMore()) {
      final I value = inputPipe.get();
      if (value != null) {
        mapper.apply(value).forEach(outputPipe::put);
      }
    }
    outputPipe.close();
  }
}
