package org.erlide.util.event_tracer;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.erlide.util.event_tracer.ErlideEvent;

@SuppressWarnings("all")
public class ErlideSessionEvent extends ErlideEvent {
  private final SimpleDateFormat formatter = new Function0<SimpleDateFormat>() {
    public SimpleDateFormat apply() {
      SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
      return _simpleDateFormat;
    }
  }.apply();
  
  public final int workspace;
  
  public ErlideSessionEvent(final String aWorkspace) {
    super(System.currentTimeMillis());
    int _hashCode = aWorkspace.hashCode();
    this.workspace = _hashCode;
  }
  
  public String print() {
    StringConcatenation _builder = new StringConcatenation();
    long _timestamp = this.getTimestamp();
    _builder.append(_timestamp, "");
    _builder.append(" SESSION ");
    long _timestamp_1 = this.getTimestamp();
    Date _date = new Date(_timestamp_1);
    String _format = this.formatter.format(_date);
    _builder.append(_format, "");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
}
