package com.exclamationlabs.connid.base.zoom.model.request;

import com.exclamationlabs.connid.base.zoom.model.ZoomPhoneBatchUser;
import java.util.List;

public class BatchAddUsers {
  List<ZoomPhoneBatchUser> users;

  public BatchAddUsers(List<ZoomPhoneBatchUser> users) {
    this.users = users;
  }

  public List<ZoomPhoneBatchUser> getUsers() {
    return users;
  }

  public void setUsers(List<ZoomPhoneBatchUser> users) {
    this.users = users;
  }
}
